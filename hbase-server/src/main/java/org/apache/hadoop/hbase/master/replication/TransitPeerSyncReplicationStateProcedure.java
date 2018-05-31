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
name|fs
operator|.
name|FileSystem
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
name|MasterFileSystem
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
name|ReopenTableRegionsProcedure
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
name|ReplicationUtils
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
name|SyncReplicationState
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
name|PeerSyncReplicationStateTransitionState
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
name|TransitPeerSyncReplicationStateStateData
import|;
end_import

begin_comment
comment|/**  * The procedure for transit current sync replication state for a synchronous replication peer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TransitPeerSyncReplicationStateProcedure
extends|extends
name|AbstractPeerProcedure
argument_list|<
name|PeerSyncReplicationStateTransitionState
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
name|TransitPeerSyncReplicationStateProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|SyncReplicationState
name|fromState
decl_stmt|;
specifier|private
name|SyncReplicationState
name|toState
decl_stmt|;
specifier|private
name|boolean
name|enabled
decl_stmt|;
specifier|public
name|TransitPeerSyncReplicationStateProcedure
parameter_list|()
block|{   }
specifier|public
name|TransitPeerSyncReplicationStateProcedure
parameter_list|(
name|String
name|peerId
parameter_list|,
name|SyncReplicationState
name|state
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
name|this
operator|.
name|toState
operator|=
name|state
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
name|TRANSIT_SYNC_REPLICATION_STATE
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
name|TransitPeerSyncReplicationStateStateData
operator|.
name|Builder
name|builder
init|=
name|TransitPeerSyncReplicationStateStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setToState
argument_list|(
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|toState
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromState
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setFromState
argument_list|(
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|fromState
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
name|TransitPeerSyncReplicationStateStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|TransitPeerSyncReplicationStateStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|toState
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|data
operator|.
name|getToState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|data
operator|.
name|hasFromState
argument_list|()
condition|)
block|{
name|fromState
operator|=
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|data
operator|.
name|getFromState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|PeerSyncReplicationStateTransitionState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|PeerSyncReplicationStateTransitionState
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
name|PeerSyncReplicationStateTransitionState
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
name|PeerSyncReplicationStateTransitionState
name|getInitialState
parameter_list|()
block|{
return|return
name|PeerSyncReplicationStateTransitionState
operator|.
name|PRE_PEER_SYNC_REPLICATION_STATE_TRANSITION
return|;
block|}
specifier|private
name|void
name|preTransit
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
name|preTransitReplicationPeerSyncReplicationState
argument_list|(
name|peerId
argument_list|,
name|toState
argument_list|)
expr_stmt|;
block|}
name|ReplicationPeerDescription
name|desc
init|=
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|preTransitPeerSyncReplicationState
argument_list|(
name|peerId
argument_list|,
name|toState
argument_list|)
decl_stmt|;
if|if
condition|(
name|toState
operator|==
name|SyncReplicationState
operator|.
name|ACTIVE
condition|)
block|{
name|Path
name|remoteWALDirForPeer
init|=
name|ReplicationUtils
operator|.
name|getPeerRemoteWALDir
argument_list|(
name|desc
operator|.
name|getPeerConfig
argument_list|()
operator|.
name|getRemoteWALDir
argument_list|()
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
comment|// check whether the remote wal directory is present
if|if
condition|(
operator|!
name|remoteWALDirForPeer
operator|.
name|getFileSystem
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|(
name|remoteWALDirForPeer
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"The remote WAL directory "
operator|+
name|remoteWALDirForPeer
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
block|}
name|fromState
operator|=
name|desc
operator|.
name|getSyncReplicationState
argument_list|()
expr_stmt|;
name|enabled
operator|=
name|desc
operator|.
name|isEnabled
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|postTransit
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
literal|"Successfully transit current cluster state from {} to {} for sync replication peer {}"
argument_list|,
name|fromState
argument_list|,
name|toState
argument_list|,
name|peerId
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
name|postTransitReplicationPeerSyncReplicationState
argument_list|(
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|reopenRegions
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getTableCFsMap
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ReopenTableRegionsProcedure
operator|::
operator|new
argument_list|)
operator|.
name|toArray
argument_list|(
name|ReopenTableRegionsProcedure
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createDirForRemoteWAL
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureYieldException
throws|,
name|IOException
block|{
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|Path
name|remoteWALDir
init|=
operator|new
name|Path
argument_list|(
name|mfs
operator|.
name|getWALRootDir
argument_list|()
argument_list|,
name|ReplicationUtils
operator|.
name|REMOTE_WAL_DIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|remoteWALDirForPeer
init|=
name|ReplicationUtils
operator|.
name|getPeerRemoteWALDir
argument_list|(
name|remoteWALDir
argument_list|,
name|peerId
argument_list|)
decl_stmt|;
name|FileSystem
name|walFs
init|=
name|mfs
operator|.
name|getWALFileSystem
argument_list|()
decl_stmt|;
if|if
condition|(
name|walFs
operator|.
name|exists
argument_list|(
name|remoteWALDirForPeer
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Wal dir {} already exists, usually this should not happen, continue anyway"
argument_list|,
name|remoteWALDirForPeer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|walFs
operator|.
name|mkdirs
argument_list|(
name|remoteWALDirForPeer
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can not create remote wal dir {}"
argument_list|,
name|remoteWALDirForPeer
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
block|}
block|}
specifier|private
name|void
name|setNextStateAfterRefreshBegin
parameter_list|()
block|{
if|if
condition|(
name|fromState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|ACTIVE
argument_list|)
condition|)
block|{
name|setNextState
argument_list|(
name|toState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|STANDBY
argument_list|)
condition|?
name|PeerSyncReplicationStateTransitionState
operator|.
name|REMOVE_ALL_REPLICATION_QUEUES_IN_PEER
else|:
name|PeerSyncReplicationStateTransitionState
operator|.
name|REOPEN_ALL_REGIONS_IN_PEER
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fromState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
condition|)
block|{
name|setNextState
argument_list|(
name|toState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|STANDBY
argument_list|)
condition|?
name|PeerSyncReplicationStateTransitionState
operator|.
name|REMOVE_ALL_REPLICATION_QUEUES_IN_PEER
else|:
name|PeerSyncReplicationStateTransitionState
operator|.
name|REOPEN_ALL_REGIONS_IN_PEER
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|toState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|DOWNGRADE_ACTIVE
argument_list|)
assert|;
name|setNextState
argument_list|(
name|PeerSyncReplicationStateTransitionState
operator|.
name|REPLAY_REMOTE_WAL_IN_PEER
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setNextStateAfterRefreshEnd
parameter_list|()
block|{
if|if
condition|(
name|toState
operator|==
name|SyncReplicationState
operator|.
name|STANDBY
condition|)
block|{
name|setNextState
argument_list|(
name|enabled
condition|?
name|PeerSyncReplicationStateTransitionState
operator|.
name|SYNC_REPLICATION_SET_PEER_ENABLED
else|:
name|PeerSyncReplicationStateTransitionState
operator|.
name|CREATE_DIR_FOR_REMOTE_WAL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setNextState
argument_list|(
name|PeerSyncReplicationStateTransitionState
operator|.
name|POST_PEER_SYNC_REPLICATION_STATE_TRANSITION
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|replayRemoteWAL
parameter_list|(
name|boolean
name|serial
parameter_list|)
block|{
name|addChildProcedure
argument_list|(
operator|new
name|RecoverStandbyProcedure
argument_list|(
name|peerId
argument_list|,
name|serial
argument_list|)
argument_list|)
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
name|PeerSyncReplicationStateTransitionState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|PRE_PEER_SYNC_REPLICATION_STATE_TRANSITION
case|:
try|try
block|{
name|preTransit
argument_list|(
name|env
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
literal|"Failed to call pre CP hook or the pre check is failed for peer {} "
operator|+
literal|"when transiting sync replication peer state to {}, "
operator|+
literal|"mark the procedure as failure and give up"
argument_list|,
name|peerId
argument_list|,
name|toState
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-transit-peer-sync-replication-state"
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
name|PeerSyncReplicationStateTransitionState
operator|.
name|SET_PEER_NEW_SYNC_REPLICATION_STATE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SET_PEER_NEW_SYNC_REPLICATION_STATE
case|:
try|try
block|{
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|setPeerNewSyncReplicationState
argument_list|(
name|peerId
argument_list|,
name|toState
argument_list|)
expr_stmt|;
if|if
condition|(
name|toState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|STANDBY
argument_list|)
operator|&&
name|enabled
condition|)
block|{
comment|// disable the peer if we are going to transit to STANDBY state, as we need to remove
comment|// all the pending replication files. If we do not disable the peer and delete the wal
comment|// queues on zk directly, RS will get NoNode exception when updating the wal position
comment|// and crash.
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|disablePeer
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
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
literal|"Failed to update peer storage for peer {} when starting transiting sync "
operator|+
literal|"replication peer state from {} to {}, retry"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
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
name|PeerSyncReplicationStateTransitionState
operator|.
name|REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_BEGIN
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_BEGIN
case|:
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|sn
lambda|->
operator|new
name|RefreshPeerProcedure
argument_list|(
name|peerId
argument_list|,
name|getPeerOperationType
argument_list|()
argument_list|,
name|sn
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|(
name|RefreshPeerProcedure
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
name|setNextStateAfterRefreshBegin
argument_list|()
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REPLAY_REMOTE_WAL_IN_PEER
case|:
name|replayRemoteWAL
argument_list|(
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|getPeerConfig
argument_list|(
name|peerId
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isSerial
argument_list|()
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|PeerSyncReplicationStateTransitionState
operator|.
name|TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REMOVE_ALL_REPLICATION_QUEUES_IN_PEER
case|:
try|try
block|{
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|removeAllQueues
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
literal|"Failed to remove all replication queues peer {} when starting transiting"
operator|+
literal|" sync replication peer state from {} to {}, retry"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
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
name|fromState
operator|.
name|equals
argument_list|(
name|SyncReplicationState
operator|.
name|ACTIVE
argument_list|)
condition|?
name|PeerSyncReplicationStateTransitionState
operator|.
name|REOPEN_ALL_REGIONS_IN_PEER
else|:
name|PeerSyncReplicationStateTransitionState
operator|.
name|TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REOPEN_ALL_REGIONS_IN_PEER
case|:
name|reopenRegions
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|PeerSyncReplicationStateTransitionState
operator|.
name|TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|TRANSIT_PEER_NEW_SYNC_REPLICATION_STATE
case|:
try|try
block|{
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|transitPeerSyncReplicationState
argument_list|(
name|peerId
argument_list|,
name|toState
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
literal|"Failed to update peer storage for peer {} when ending transiting sync "
operator|+
literal|"replication peer state from {} to {}, retry"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
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
name|PeerSyncReplicationStateTransitionState
operator|.
name|REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_END
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REFRESH_PEER_SYNC_REPLICATION_STATE_ON_RS_END
case|:
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|sn
lambda|->
operator|new
name|RefreshPeerProcedure
argument_list|(
name|peerId
argument_list|,
name|getPeerOperationType
argument_list|()
argument_list|,
name|sn
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|(
name|RefreshPeerProcedure
index|[]
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
name|setNextStateAfterRefreshEnd
argument_list|()
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SYNC_REPLICATION_SET_PEER_ENABLED
case|:
try|try
block|{
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|enablePeer
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
literal|"Failed to set peer enabled for peer {} when transiting sync replication peer "
operator|+
literal|"state from {} to {}, retry"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
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
name|PeerSyncReplicationStateTransitionState
operator|.
name|SYNC_REPLICATION_ENABLE_PEER_REFRESH_PEER_ON_RS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SYNC_REPLICATION_ENABLE_PEER_REFRESH_PEER_ON_RS
case|:
name|refreshPeer
argument_list|(
name|env
argument_list|,
name|PeerOperationType
operator|.
name|ENABLE
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|PeerSyncReplicationStateTransitionState
operator|.
name|CREATE_DIR_FOR_REMOTE_WAL
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|CREATE_DIR_FOR_REMOTE_WAL
case|:
try|try
block|{
name|createDirForRemoteWAL
argument_list|(
name|env
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
literal|"Failed to create remote wal dir for peer {} when transiting sync replication "
operator|+
literal|"peer state from {} to {}, retry"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
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
name|PeerSyncReplicationStateTransitionState
operator|.
name|POST_PEER_SYNC_REPLICATION_STATE_TRANSITION
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|POST_PEER_SYNC_REPLICATION_STATE_TRANSITION
case|:
try|try
block|{
name|postTransit
argument_list|(
name|env
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
literal|"Failed to call post CP hook for peer {} when transiting sync replication "
operator|+
literal|"peer state from {} to {}, ignore since the procedure has already done"
argument_list|,
name|peerId
argument_list|,
name|fromState
argument_list|,
name|toState
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

