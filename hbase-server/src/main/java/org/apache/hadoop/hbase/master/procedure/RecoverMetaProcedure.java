begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|procedure
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
name|Set
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
name|RegionInfo
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
name|RegionInfoBuilder
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
name|RegionReplicaUtil
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
name|MasterServices
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
name|assignment
operator|.
name|AssignProcedure
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
name|procedure2
operator|.
name|StateMachineProcedure
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
name|MetaTableLocator
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
name|base
operator|.
name|Preconditions
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
name|RecoverMetaState
import|;
end_import

begin_comment
comment|/**  * This procedure recovers meta from prior shutdown/ crash of a server, and brings meta online by  * assigning meta region/s. Any place where meta is accessed and requires meta to be online, need to  * submit this procedure instead of duplicating steps to recover meta in the code.  */
end_comment

begin_class
specifier|public
class|class
name|RecoverMetaProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
argument_list|>
implements|implements
name|TableProcedureInterface
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
name|RecoverMetaProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ServerName
name|failedMetaServer
decl_stmt|;
specifier|private
name|boolean
name|shouldSplitWal
decl_stmt|;
specifier|private
name|int
name|replicaId
decl_stmt|;
specifier|private
specifier|final
name|ProcedurePrepareLatch
name|syncLatch
decl_stmt|;
specifier|private
name|MasterServices
name|master
decl_stmt|;
comment|/**    * Call this constructor to queue up a {@link RecoverMetaProcedure} in response to meta    * carrying server crash    * @param failedMetaServer failed/ crashed region server that was carrying meta    * @param shouldSplitLog split log file of meta region    */
specifier|public
name|RecoverMetaProcedure
parameter_list|(
specifier|final
name|ServerName
name|failedMetaServer
parameter_list|,
specifier|final
name|boolean
name|shouldSplitLog
parameter_list|)
block|{
name|this
argument_list|(
name|failedMetaServer
argument_list|,
name|shouldSplitLog
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor with latch, for blocking/ sync usage    */
specifier|public
name|RecoverMetaProcedure
parameter_list|(
specifier|final
name|ServerName
name|failedMetaServer
parameter_list|,
specifier|final
name|boolean
name|shouldSplitLog
parameter_list|,
specifier|final
name|ProcedurePrepareLatch
name|latch
parameter_list|)
block|{
name|this
operator|.
name|failedMetaServer
operator|=
name|failedMetaServer
expr_stmt|;
name|this
operator|.
name|shouldSplitWal
operator|=
name|shouldSplitLog
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
expr_stmt|;
name|this
operator|.
name|syncLatch
operator|=
name|latch
expr_stmt|;
block|}
comment|/**    * This constructor is also used when deserializing from a procedure store; we'll construct one    * of these then call #deserializeStateData(InputStream). Do not use directly.    */
specifier|public
name|RecoverMetaProcedure
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|false
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
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
name|prepare
argument_list|(
name|env
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isRunRequired
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|+
literal|"; Meta already initialized. Skipping run"
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|RECOVER_META_SPLIT_LOGS
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Start "
operator|+
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|shouldSplitWal
condition|)
block|{
comment|// TODO: Matteo. We BLOCK here but most important thing to be doing at this moment.
if|if
condition|(
name|failedMetaServer
operator|!=
literal|null
condition|)
block|{
name|master
operator|.
name|getMasterWalManager
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|failedMetaServer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ServerName
name|serverName
init|=
name|master
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|getMetaRegionLocation
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ServerName
argument_list|>
name|previouslyFailedServers
init|=
name|master
operator|.
name|getMasterWalManager
argument_list|()
operator|.
name|getFailedServersFromLogFolders
argument_list|()
decl_stmt|;
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
name|previouslyFailedServers
operator|.
name|contains
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|master
operator|.
name|getMasterWalManager
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|setNextState
argument_list|(
name|RecoverMetaState
operator|.
name|RECOVER_META_ASSIGN_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|RECOVER_META_ASSIGN_REGIONS
case|:
name|RegionInfo
name|hri
init|=
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|this
operator|.
name|replicaId
argument_list|)
decl_stmt|;
name|AssignProcedure
name|metaAssignProcedure
decl_stmt|;
if|if
condition|(
name|failedMetaServer
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|+
literal|"; Assigning meta with new plan. previous meta server="
operator|+
name|failedMetaServer
argument_list|)
expr_stmt|;
name|metaAssignProcedure
operator|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createAssignProcedure
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// get server carrying meta from zk
name|ServerName
name|metaServer
init|=
name|MetaTableLocator
operator|.
name|getMetaRegionState
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|+
literal|"; Retaining meta assignment to server="
operator|+
name|metaServer
argument_list|)
expr_stmt|;
name|metaAssignProcedure
operator|=
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createAssignProcedure
argument_list|(
name|hri
argument_list|,
name|metaServer
argument_list|)
expr_stmt|;
block|}
name|addChildProcedure
argument_list|(
name|metaAssignProcedure
argument_list|)
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
catch|catch
parameter_list|(
name|IOException
decl||
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|this
operator|+
literal|"; Failed state="
operator|+
name|state
operator|+
literal|", retry "
operator|+
name|this
operator|+
literal|"; cycles="
operator|+
name|getCycles
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
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
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
name|recoverMetaState
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Can't rollback
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"unhandled state="
operator|+
name|recoverMetaState
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|RecoverMetaState
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
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
name|recoverMetaState
parameter_list|)
block|{
return|return
name|recoverMetaState
operator|.
name|getNumber
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|MasterProcedureProtos
operator|.
name|RecoverMetaState
name|getInitialState
parameter_list|()
block|{
return|return
name|RecoverMetaState
operator|.
name|RECOVER_META_SPLIT_LOGS
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|toStringClassDetails
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" failedMetaServer="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|failedMetaServer
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", splitWal="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|shouldSplitWal
argument_list|)
expr_stmt|;
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
name|MasterProcedureProtos
operator|.
name|RecoverMetaStateData
operator|.
name|Builder
name|state
init|=
name|MasterProcedureProtos
operator|.
name|RecoverMetaStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setShouldSplitWal
argument_list|(
name|shouldSplitWal
argument_list|)
decl_stmt|;
if|if
condition|(
name|failedMetaServer
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|setFailedMetaServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|failedMetaServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|state
operator|.
name|setReplicaId
argument_list|(
name|replicaId
argument_list|)
expr_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|state
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
name|MasterProcedureProtos
operator|.
name|RecoverMetaStateData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|RecoverMetaStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|shouldSplitWal
operator|=
name|state
operator|.
name|hasShouldSplitWal
argument_list|()
operator|&&
name|state
operator|.
name|getShouldSplitWal
argument_list|()
expr_stmt|;
name|this
operator|.
name|failedMetaServer
operator|=
name|state
operator|.
name|hasFailedMetaServer
argument_list|()
condition|?
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|state
operator|.
name|getFailedMetaServer
argument_list|()
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|replicaId
operator|=
name|state
operator|.
name|hasReplicaId
argument_list|()
condition|?
name|state
operator|.
name|getReplicaId
argument_list|()
else|:
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|LockState
name|acquireLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitTableExclusiveLock
argument_list|(
name|this
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
return|return
name|LockState
operator|.
name|LOCK_ACQUIRED
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeTableExclusiveLock
argument_list|(
name|this
argument_list|,
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|completionCleanup
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|syncLatch
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|META_TABLE_NAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableOperationType
name|getTableOperationType
parameter_list|()
block|{
return|return
name|TableOperationType
operator|.
name|ENABLE
return|;
block|}
comment|/**    * @return true if failedMetaServer is not null (meta carrying server crashed) or meta is    * already initialized    */
specifier|private
name|boolean
name|isRunRequired
parameter_list|()
block|{
return|return
name|failedMetaServer
operator|!=
literal|null
operator|||
operator|!
name|master
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isMetaInitialized
argument_list|()
return|;
block|}
comment|/**    * Prepare for execution    */
specifier|private
name|void
name|prepare
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|master
operator|==
literal|null
condition|)
block|{
name|master
operator|=
name|env
operator|.
name|getMasterServices
argument_list|()
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|master
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

