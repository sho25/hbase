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
name|io
operator|.
name|InputStream
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
name|Iterator
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
name|hbase
operator|.
name|HRegionInfo
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
name|MasterWalManager
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
name|master
operator|.
name|assignment
operator|.
name|AssignmentManager
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
name|RegionTransitionProcedure
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
name|ProcedureMetrics
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
name|HBaseProtos
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
name|ServerCrashState
import|;
end_import

begin_comment
comment|/**  * Handle crashed server. This is a port to ProcedureV2 of what used to be euphemistically called  * ServerShutdownHandler.  *  *<p>The procedure flow varies dependent on whether meta is assigned and if we are to split logs.  *  *<p>We come in here after ServerManager has noticed a server has expired. Procedures  * queued on the rpc should have been notified about fail and should be concurrently  * getting themselves ready to assign elsewhere.  */
end_comment

begin_class
specifier|public
class|class
name|ServerCrashProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|ServerCrashState
argument_list|>
implements|implements
name|ServerProcedureInterface
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
name|ServerCrashProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Name of the crashed server to process.    */
specifier|private
name|ServerName
name|serverName
decl_stmt|;
comment|/**    * Whether DeadServer knows that we are processing it.    */
specifier|private
name|boolean
name|notifiedDeadServer
init|=
literal|false
decl_stmt|;
comment|/**    * Regions that were on the crashed server.    */
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsOnCrashedServer
decl_stmt|;
specifier|private
name|boolean
name|carryingMeta
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|shouldSplitWal
decl_stmt|;
comment|/**    * Call this constructor queuing up a Procedure.    * @param serverName Name of the crashed server.    * @param shouldSplitWal True if we should split WALs as part of crashed server processing.    * @param carryingMeta True if carrying hbase:meta table region.    */
specifier|public
name|ServerCrashProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|boolean
name|shouldSplitWal
parameter_list|,
specifier|final
name|boolean
name|carryingMeta
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|shouldSplitWal
operator|=
name|shouldSplitWal
expr_stmt|;
name|this
operator|.
name|carryingMeta
operator|=
name|carryingMeta
expr_stmt|;
name|this
operator|.
name|setOwner
argument_list|(
name|env
operator|.
name|getRequestUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Used when deserializing from a procedure store; we'll construct one of these then call    * #deserializeStateData(InputStream). Do not use directly.    */
specifier|public
name|ServerCrashProcedure
parameter_list|()
block|{
name|super
argument_list|()
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
name|ServerCrashState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
block|{
specifier|final
name|MasterServices
name|services
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
decl_stmt|;
comment|// HBASE-14802
comment|// If we have not yet notified that we are processing a dead server, we should do now.
if|if
condition|(
operator|!
name|notifiedDeadServer
condition|)
block|{
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getDeadServers
argument_list|()
operator|.
name|notifyServer
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|notifiedDeadServer
operator|=
literal|true
expr_stmt|;
block|}
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|SERVER_CRASH_START
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
comment|// If carrying meta, process it first. Else, get list of regions on crashed server.
if|if
condition|(
name|this
operator|.
name|carryingMeta
condition|)
block|{
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_PROCESS_META
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_GET_REGIONS
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|SERVER_CRASH_GET_REGIONS
case|:
comment|// If hbase:meta is not assigned, yield.
if|if
condition|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|waitMetaLoaded
argument_list|(
name|this
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
name|this
operator|.
name|regionsOnCrashedServer
operator|=
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerRegionInfoSet
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
comment|// Where to go next? Depends on whether we should split logs at all or
comment|// if we should do distributed log splitting.
if|if
condition|(
operator|!
name|this
operator|.
name|shouldSplitWal
condition|)
block|{
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_ASSIGN
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_SPLIT_LOGS
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|SERVER_CRASH_PROCESS_META
case|:
name|processMeta
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_GET_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|SERVER_CRASH_SPLIT_LOGS
case|:
name|splitLogs
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_ASSIGN
argument_list|)
expr_stmt|;
break|break;
case|case
name|SERVER_CRASH_ASSIGN
case|:
comment|// If no regions to assign, skip assign and skip to the finish.
comment|// Filter out meta regions. Those are handled elsewhere in this procedure.
comment|// Filter changes this.regionsOnCrashedServer.
if|if
condition|(
name|filterDefaultMetaRegions
argument_list|(
name|regionsOnCrashedServer
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Assigning regions "
operator|+
name|HRegionInfo
operator|.
name|getShortNameToLog
argument_list|(
name|regionsOnCrashedServer
argument_list|)
operator|+
literal|", "
operator|+
name|this
operator|+
literal|"; cycles="
operator|+
name|getCycles
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|handleRIT
argument_list|(
name|env
argument_list|,
name|regionsOnCrashedServer
argument_list|)
expr_stmt|;
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
comment|// forceNewPlan is set to false. Balancer is expected to find most suitable target
comment|// server if retention is not possible.
name|addChildProcedure
argument_list|(
name|am
operator|.
name|createAssignProcedures
argument_list|(
name|am
operator|.
name|getOrderedRegions
argument_list|(
name|regionsOnCrashedServer
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|setNextState
argument_list|(
name|ServerCrashState
operator|.
name|SERVER_CRASH_FINISH
argument_list|)
expr_stmt|;
break|break;
case|case
name|SERVER_CRASH_FINISH
case|:
name|services
operator|.
name|getServerManager
argument_list|()
operator|.
name|getDeadServers
argument_list|()
operator|.
name|finish
argument_list|(
name|serverName
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
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed state="
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
comment|/**    * @param env    * @throws IOException    */
specifier|private
name|void
name|processMeta
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
name|LOG
operator|.
name|debug
argument_list|(
name|this
operator|+
literal|"; Processing hbase:meta that was on "
operator|+
name|this
operator|.
name|serverName
argument_list|)
expr_stmt|;
comment|// Assign meta if still carrying it. Check again: region may be assigned because of RIT timeout
specifier|final
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getServerRegionInfoSet
argument_list|(
name|serverName
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|isDefaultMetaRegion
argument_list|(
name|hri
argument_list|)
condition|)
continue|continue;
name|am
operator|.
name|offlineRegion
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|addChildProcedure
argument_list|(
operator|new
name|RecoverMetaProcedure
argument_list|(
name|serverName
argument_list|,
name|this
operator|.
name|shouldSplitWal
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|filterDefaultMetaRegions
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
if|if
condition|(
name|regions
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|regions
operator|.
name|removeIf
argument_list|(
name|this
operator|::
name|isDefaultMetaRegion
argument_list|)
expr_stmt|;
return|return
operator|!
name|regions
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|isDefaultMetaRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
block|{
return|return
name|hri
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|&&
name|RegionReplicaUtil
operator|.
name|isDefaultReplica
argument_list|(
name|hri
argument_list|)
return|;
block|}
specifier|private
name|void
name|splitLogs
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Splitting WALs "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
name|MasterWalManager
name|mwm
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterWalManager
argument_list|()
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
comment|// TODO: For Matteo. Below BLOCKs!!!! Redo so can relinquish executor while it is running.
comment|// PROBLEM!!! WE BLOCK HERE.
name|mwm
operator|.
name|splitLog
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Done splitting WALs "
operator|+
name|this
argument_list|)
expr_stmt|;
block|}
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|logSplit
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
expr_stmt|;
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
name|ServerCrashState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Can't rollback.
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
annotation|@
name|Override
specifier|protected
name|ServerCrashState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ServerCrashState
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
name|ServerCrashState
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
name|ServerCrashState
name|getInitialState
parameter_list|()
block|{
return|return
name|ServerCrashState
operator|.
name|SERVER_CRASH_START
return|;
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
comment|// TODO
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|LockState
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// TODO: Put this BACK AFTER AMv2 goes in!!!!
comment|// if (env.waitFailoverCleanup(this)) return LockState.LOCK_EVENT_WAIT;
if|if
condition|(
name|env
operator|.
name|waitServerCrashProcessingEnabled
argument_list|(
name|this
argument_list|)
condition|)
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitServerExclusiveLock
argument_list|(
name|this
argument_list|,
name|getServerName
argument_list|()
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
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|wakeServerExclusiveLock
argument_list|(
name|this
argument_list|,
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
literal|" server="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|serverName
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
name|sb
operator|.
name|append
argument_list|(
literal|", meta="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|carryingMeta
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
name|ServerCrashStateData
operator|.
name|Builder
name|state
init|=
name|MasterProcedureProtos
operator|.
name|ServerCrashStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServerName
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
argument_list|)
operator|.
name|setCarryingMeta
argument_list|(
name|this
operator|.
name|carryingMeta
argument_list|)
operator|.
name|setShouldSplitWal
argument_list|(
name|this
operator|.
name|shouldSplitWal
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|regionsOnCrashedServer
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|regionsOnCrashedServer
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|this
operator|.
name|regionsOnCrashedServer
control|)
block|{
name|state
operator|.
name|addRegionsOnCrashedServer
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|ServerCrashStateData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|ServerCrashStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|serverName
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|state
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|carryingMeta
operator|=
name|state
operator|.
name|hasCarryingMeta
argument_list|()
condition|?
name|state
operator|.
name|getCarryingMeta
argument_list|()
else|:
literal|false
expr_stmt|;
comment|// shouldSplitWAL has a default over in pb so this invocation will always work.
name|this
operator|.
name|shouldSplitWal
operator|=
name|state
operator|.
name|getShouldSplitWal
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|state
operator|.
name|getRegionsOnCrashedServerCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|regionsOnCrashedServer
operator|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|state
operator|.
name|getRegionsOnCrashedServerList
argument_list|()
control|)
block|{
name|this
operator|.
name|regionsOnCrashedServer
operator|.
name|add
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|ri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|serverName
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasMetaTableRegion
parameter_list|()
block|{
return|return
name|this
operator|.
name|carryingMeta
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerOperationType
name|getServerOperationType
parameter_list|()
block|{
return|return
name|ServerOperationType
operator|.
name|CRASH_HANDLER
return|;
block|}
comment|/**    * For this procedure, yield at end of each successful flow step so that all crashed servers    * can make progress rather than do the default which has each procedure running to completion    * before we move to the next. For crashed servers, especially if running with distributed log    * replay, we will want all servers to come along; we do not want the scenario where a server is    * stuck waiting for regions to online so it can replay edits.    */
annotation|@
name|Override
specifier|protected
name|boolean
name|isYieldBeforeExecuteFromState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerCrashState
name|state
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldWaitClientAck
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// The operation is triggered internally on the server
comment|// the client does not know about this procedure.
return|return
literal|false
return|;
block|}
comment|/**    * Handle any outstanding RIT that are up against this.serverName, the crashed server.    * Notify them of crash. Remove assign entries from the passed in<code>regions</code>    * otherwise we have two assigns going on and they will fight over who has lock.    * Notify Unassigns. If unable to unassign because server went away, unassigns block waiting    * on the below callback from a ServerCrashProcedure before proceeding.    * @param env    * @param regions Regions that were on crashed server    */
specifier|private
name|void
name|handleRIT
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
if|if
condition|(
name|regions
operator|==
literal|null
condition|)
return|return;
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
specifier|final
name|Iterator
argument_list|<
name|HRegionInfo
argument_list|>
name|it
init|=
name|regions
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|ServerCrashException
name|sce
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|HRegionInfo
name|hri
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|RegionTransitionProcedure
name|rtp
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionTransitionProcedure
argument_list|(
name|hri
argument_list|)
decl_stmt|;
if|if
condition|(
name|rtp
operator|==
literal|null
condition|)
continue|continue;
comment|// Make sure the RIT is against this crashed server. In the case where there are many
comment|// processings of a crashed server -- backed up for whatever reason (slow WAL split) --
comment|// then a previous SCP may have already failed an assign, etc., and it may have a new
comment|// location target; DO NOT fail these else we make for assign flux.
name|ServerName
name|rtpServerName
init|=
name|rtp
operator|.
name|getServer
argument_list|(
name|env
argument_list|)
decl_stmt|;
if|if
condition|(
name|rtpServerName
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"RIT with ServerName null! "
operator|+
name|rtp
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|rtpServerName
operator|.
name|equals
argument_list|(
name|this
operator|.
name|serverName
argument_list|)
condition|)
continue|continue;
name|LOG
operator|.
name|info
argument_list|(
literal|"pid="
operator|+
name|getProcId
argument_list|()
operator|+
literal|" found RIT "
operator|+
name|rtp
operator|+
literal|"; "
operator|+
name|rtp
operator|.
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Notify RIT on server crash.
if|if
condition|(
name|sce
operator|==
literal|null
condition|)
block|{
name|sce
operator|=
operator|new
name|ServerCrashException
argument_list|(
name|getProcId
argument_list|()
argument_list|,
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rtp
operator|.
name|remoteCallFailed
argument_list|(
name|env
argument_list|,
name|this
operator|.
name|serverName
argument_list|,
name|sce
argument_list|)
expr_stmt|;
if|if
condition|(
name|rtp
operator|instanceof
name|AssignProcedure
condition|)
block|{
comment|// If an assign, include it in our return and remove from passed-in list of regions.
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|ProcedureMetrics
name|getProcedureMetrics
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterMetrics
argument_list|()
operator|.
name|getServerCrashProcMetrics
argument_list|()
return|;
block|}
block|}
end_class

end_unit

