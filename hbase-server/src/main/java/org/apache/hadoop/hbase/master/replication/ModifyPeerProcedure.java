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
name|io
operator|.
name|InterruptedIOException
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
name|Map
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
name|MetaTableAccessor
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Connection
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
name|TableDescriptor
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
name|TableState
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
name|TableStateManager
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
name|TableStateManager
operator|.
name|TableStateNotFoundException
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
name|ProcedurePrepareLatch
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
name|ProcedureUtil
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
name|hadoop
operator|.
name|hbase
operator|.
name|replication
operator|.
name|ReplicationQueueStorage
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
name|util
operator|.
name|Pair
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
name|PeerModificationState
import|;
end_import

begin_comment
comment|/**  * The base class for all replication peer related procedure except sync replication state  * transition.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ModifyPeerProcedure
extends|extends
name|AbstractPeerProcedure
argument_list|<
name|PeerModificationState
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
name|ModifyPeerProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|UPDATE_LAST_SEQ_ID_BATCH_SIZE
init|=
literal|1000
decl_stmt|;
comment|// The sleep interval when waiting table to be enabled or disabled.
specifier|protected
specifier|static
specifier|final
name|int
name|SLEEP_INTERVAL_MS
init|=
literal|1000
decl_stmt|;
specifier|protected
name|ModifyPeerProcedure
parameter_list|()
block|{   }
specifier|protected
name|ModifyPeerProcedure
parameter_list|(
name|String
name|peerId
parameter_list|)
block|{
name|super
argument_list|(
name|peerId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Called before we start the actual processing. The implementation should call the pre CP hook,    * and also the pre-check for the peer modification.    *<p>    * If an IOException is thrown then we will give up and mark the procedure as failed directly. If    * all checks passes then the procedure can not be rolled back any more.    */
specifier|protected
specifier|abstract
name|void
name|prePeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|updatePeerStorage
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ReplicationException
function_decl|;
comment|/**    * Called before we finish the procedure. The implementation can do some logging work, and also    * call the coprocessor hook if any.    *<p>    * Notice that, since we have already done the actual work, throwing {@code IOException} here will    * not fail this procedure, we will just ignore it and finish the procedure as suceeded. If    * {@code ReplicationException} is thrown we will retry since this usually means we fails to    * update the peer storage.    */
specifier|protected
specifier|abstract
name|void
name|postPeerModification
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
function_decl|;
specifier|private
name|void
name|releaseLatch
parameter_list|()
block|{
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|latch
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Implementation class can override this method. By default we will jump to    * POST_PEER_MODIFICATION and finish the procedure.    */
specifier|protected
name|PeerModificationState
name|nextStateAfterRefresh
parameter_list|()
block|{
return|return
name|PeerModificationState
operator|.
name|POST_PEER_MODIFICATION
return|;
block|}
comment|/**    * The implementation class should override this method if the procedure may enter the serial    * related states.    */
specifier|protected
name|boolean
name|enablePeerBeforeFinish
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|protected
name|ReplicationPeerConfig
name|getOldPeerConfig
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|protected
name|ReplicationPeerConfig
name|getNewPeerConfig
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|protected
name|void
name|updateLastPushedSequenceIdForSerialPeer
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|// If the table is in enabling state, we need to wait until it is enabled and then reopen all its
comment|// regions.
specifier|private
name|boolean
name|needReopen
parameter_list|(
name|TableStateManager
name|tsm
parameter_list|,
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
try|try
block|{
name|TableState
name|state
init|=
name|tsm
operator|.
name|getTableState
argument_list|(
name|tn
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|.
name|isEnabled
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|state
operator|.
name|isEnabling
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL_MS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableStateNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|IOException
operator|)
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// will be override in test to simulate error
annotation|@
name|VisibleForTesting
specifier|protected
name|void
name|reopenRegions
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|getNewPeerConfig
argument_list|()
decl_stmt|;
name|ReplicationPeerConfig
name|oldPeerConfig
init|=
name|getOldPeerConfig
argument_list|()
decl_stmt|;
name|TableStateManager
name|tsm
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|td
range|:
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|getAll
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|td
operator|.
name|hasGlobalReplicationScope
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|TableName
name|tn
init|=
name|td
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ReplicationUtils
operator|.
name|contains
argument_list|(
name|peerConfig
argument_list|,
name|tn
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|oldPeerConfig
operator|!=
literal|null
operator|&&
name|oldPeerConfig
operator|.
name|isSerial
argument_list|()
operator|&&
name|ReplicationUtils
operator|.
name|contains
argument_list|(
name|oldPeerConfig
argument_list|,
name|tn
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|needReopen
argument_list|(
name|tsm
argument_list|,
name|tn
argument_list|)
condition|)
block|{
name|addChildProcedure
argument_list|(
operator|new
name|ReopenTableRegionsProcedure
argument_list|(
name|tn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|addToMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastSeqIds
parameter_list|,
name|String
name|encodedRegionName
parameter_list|,
name|long
name|barrier
parameter_list|,
name|ReplicationQueueStorage
name|queueStorage
parameter_list|)
throws|throws
name|ReplicationException
block|{
if|if
condition|(
name|barrier
operator|>=
literal|0
condition|)
block|{
name|lastSeqIds
operator|.
name|put
argument_list|(
name|encodedRegionName
argument_list|,
name|barrier
argument_list|)
expr_stmt|;
if|if
condition|(
name|lastSeqIds
operator|.
name|size
argument_list|()
operator|>=
name|UPDATE_LAST_SEQ_ID_BATCH_SIZE
condition|)
block|{
name|queueStorage
operator|.
name|setLastSequenceIds
argument_list|(
name|peerId
argument_list|,
name|lastSeqIds
argument_list|)
expr_stmt|;
name|lastSeqIds
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|final
name|void
name|setLastPushedSequenceId
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastSeqIds
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDescriptor
name|td
range|:
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|getAll
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|td
operator|.
name|hasGlobalReplicationScope
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|TableName
name|tn
init|=
name|td
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ReplicationUtils
operator|.
name|contains
argument_list|(
name|peerConfig
argument_list|,
name|tn
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|setLastPushedSequenceIdForTable
argument_list|(
name|env
argument_list|,
name|tn
argument_list|,
name|lastSeqIds
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lastSeqIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|getQueueStorage
argument_list|()
operator|.
name|setLastSequenceIds
argument_list|(
name|peerId
argument_list|,
name|lastSeqIds
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If the table is currently disabling, then we need to wait until it is disabled.We will write
comment|// replication barrier for a disabled table. And return whether we need to update the last pushed
comment|// sequence id, if the table has been deleted already, i.e, we hit TableStateNotFoundException,
comment|// then we do not need to update last pushed sequence id for this table.
specifier|private
name|boolean
name|needSetLastPushedSequenceId
parameter_list|(
name|TableStateManager
name|tsm
parameter_list|,
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
try|try
block|{
if|if
condition|(
operator|!
name|tsm
operator|.
name|getTableState
argument_list|(
name|tn
argument_list|)
operator|.
name|isDisabling
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
name|SLEEP_INTERVAL_MS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableStateNotFoundException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|IOException
operator|)
operator|new
name|InterruptedIOException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|// Will put the encodedRegionName->lastPushedSeqId pair into the map passed in, if the map is
comment|// large enough we will call queueStorage.setLastSequenceIds and clear the map. So the caller
comment|// should not forget to check whether the map is empty at last, if not you should call
comment|// queueStorage.setLastSequenceIds to write out the remaining entries in the map.
specifier|protected
specifier|final
name|void
name|setLastPushedSequenceIdForTable
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|lastSeqIds
parameter_list|)
throws|throws
name|IOException
throws|,
name|ReplicationException
block|{
name|TableStateManager
name|tsm
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
decl_stmt|;
name|ReplicationQueueStorage
name|queueStorage
init|=
name|env
operator|.
name|getReplicationPeerManager
argument_list|()
operator|.
name|getQueueStorage
argument_list|()
decl_stmt|;
name|Connection
name|conn
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|needSetLastPushedSequenceId
argument_list|(
name|tsm
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skip settting last pushed sequence id for {}"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|Pair
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|name2Barrier
range|:
name|MetaTableAccessor
operator|.
name|getTableEncodedRegionNameAndLastBarrier
argument_list|(
name|conn
argument_list|,
name|tableName
argument_list|)
control|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Update last pushed sequence id for {}, {}"
argument_list|,
name|tableName
argument_list|,
name|name2Barrier
argument_list|)
expr_stmt|;
name|addToMap
argument_list|(
name|lastSeqIds
argument_list|,
name|name2Barrier
operator|.
name|getFirst
argument_list|()
argument_list|,
name|name2Barrier
operator|.
name|getSecond
argument_list|()
operator|.
name|longValue
argument_list|()
operator|-
literal|1
argument_list|,
name|queueStorage
argument_list|)
expr_stmt|;
block|}
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
name|PeerModificationState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|PRE_PEER_MODIFICATION
case|:
try|try
block|{
name|prePeerModification
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
literal|"{} failed to call pre CP hook or the pre check is failed for peer {}, "
operator|+
literal|"mark the procedure as failure and give up"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-"
operator|+
name|getPeerOperationType
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"-peer"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|releaseLatch
argument_list|()
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} failed to call prePeerModification for peer {}, sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
block|}
name|attempts
operator|=
literal|0
expr_stmt|;
name|setNextState
argument_list|(
name|PeerModificationState
operator|.
name|UPDATE_PEER_STORAGE
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|UPDATE_PEER_STORAGE
case|:
try|try
block|{
name|updatePeerStorage
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} update peer storage for peer {} failed, sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
block|}
name|attempts
operator|=
literal|0
expr_stmt|;
name|setNextState
argument_list|(
name|PeerModificationState
operator|.
name|REFRESH_PEER_ON_RS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REFRESH_PEER_ON_RS
case|:
name|refreshPeer
argument_list|(
name|env
argument_list|,
name|getPeerOperationType
argument_list|()
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|nextStateAfterRefresh
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SERIAL_PEER_REOPEN_REGIONS
case|:
try|try
block|{
name|reopenRegions
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} reopen regions for peer {} failed,  sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
block|}
name|attempts
operator|=
literal|0
expr_stmt|;
name|setNextState
argument_list|(
name|PeerModificationState
operator|.
name|SERIAL_PEER_UPDATE_LAST_PUSHED_SEQ_ID
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SERIAL_PEER_UPDATE_LAST_PUSHED_SEQ_ID
case|:
try|try
block|{
name|updateLastPushedSequenceIdForSerialPeer
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} set last sequence id for peer {} failed,  sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
block|}
name|attempts
operator|=
literal|0
expr_stmt|;
name|setNextState
argument_list|(
name|enablePeerBeforeFinish
argument_list|()
condition|?
name|PeerModificationState
operator|.
name|SERIAL_PEER_SET_PEER_ENABLED
else|:
name|PeerModificationState
operator|.
name|POST_PEER_MODIFICATION
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SERIAL_PEER_SET_PEER_ENABLED
case|:
try|try
block|{
name|enablePeer
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} enable peer before finish for peer {} failed,  sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
block|}
name|attempts
operator|=
literal|0
expr_stmt|;
name|setNextState
argument_list|(
name|PeerModificationState
operator|.
name|SERIAL_PEER_ENABLE_PEER_REFRESH_PEER_ON_RS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|SERIAL_PEER_ENABLE_PEER_REFRESH_PEER_ON_RS
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
name|PeerModificationState
operator|.
name|POST_PEER_MODIFICATION
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|POST_PEER_MODIFICATION
case|:
try|try
block|{
name|postPeerModification
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|ProcedureUtil
operator|.
name|getBackoffTimeMs
argument_list|(
name|attempts
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"{} failed to call postPeerModification for peer {},  sleep {} secs"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|suspend
argument_list|(
name|backoff
argument_list|)
throw|;
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
literal|"{} failed to call post CP hook for peer {}, "
operator|+
literal|"ignore since the procedure has already done"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|peerId
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|releaseLatch
argument_list|()
expr_stmt|;
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
name|PeerModificationState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|PeerModificationState
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
name|PeerModificationState
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
name|PeerModificationState
name|getInitialState
parameter_list|()
block|{
return|return
name|PeerModificationState
operator|.
name|PRE_PEER_MODIFICATION
return|;
block|}
block|}
end_class

end_unit

