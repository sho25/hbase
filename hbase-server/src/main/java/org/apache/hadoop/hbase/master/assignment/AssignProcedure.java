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
name|master
operator|.
name|assignment
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
name|Comparator
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
name|RetriesExhaustedException
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
name|exceptions
operator|.
name|UnexpectedStateException
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
name|RegionState
operator|.
name|State
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
name|assignment
operator|.
name|RegionStates
operator|.
name|RegionStateNode
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
name|RSProcedureDispatcher
operator|.
name|RegionOpenOperation
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
name|AssignRegionStateData
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
name|RegionTransitionState
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
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
import|;
end_import

begin_comment
comment|/**  * Procedure that describe the assignment of a single region.  * There can only be one RegionTransitionProcedure per region running at a time  * since each procedure takes a lock on the region.  *  *<p>The Assign starts by pushing the "assign" operation to the AssignmentManager  * and then will go in a "waiting" state.  * The AM will batch the "assign" requests and ask the Balancer where to put  * the region (the various policies will be respected: retain, round-robin, random).  * Once the AM and the balancer have found a place for the region the procedure  * will be resumed and an "open region" request will be placed in the Remote Dispatcher  * queue, and the procedure once again will go in a "waiting state".  * The Remote Dispatcher will batch the various requests for that server and  * they will be sent to the RS for execution.  * The RS will complete the open operation by calling master.reportRegionStateTransition().  * The AM will intercept the transition report, and notify the procedure.  * The procedure will finish the assignment by publishing to new state on meta  * or it will retry the assignment.  *  *<p>This procedure does not rollback when beyond the first  * REGION_TRANSITION_QUEUE step; it will press on trying to assign in the face of  * failure. Should we ignore rollback calls to Assign/Unassign then? Or just  * remove rollback here?  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AssignProcedure
extends|extends
name|RegionTransitionProcedure
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
name|AssignProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Set to true when we need recalibrate -- choose a new target -- because original assign failed.    */
specifier|private
name|boolean
name|forceNewPlan
init|=
literal|false
decl_stmt|;
comment|/**    * Gets set as desired target on move, merge, etc., when we want to go to a particular server.    * We may not be able to respect this request but will try. When it is NOT set, then we ask    * the balancer to assign. This value is used below in startTransition to set regionLocation if    * non-null. Setting regionLocation in regionServerNode is how we override balancer setting    * destination.    */
specifier|protected
specifier|volatile
name|ServerName
name|targetServer
decl_stmt|;
comment|/**    * Comparator that will sort AssignProcedures so meta assigns come first, then system table    * assigns and finally user space assigns.    */
specifier|public
specifier|static
specifier|final
name|CompareAssignProcedure
name|COMPARATOR
init|=
operator|new
name|CompareAssignProcedure
argument_list|()
decl_stmt|;
specifier|public
name|AssignProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AssignProcedure
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetServer
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|AssignProcedure
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|ServerName
name|destinationServer
parameter_list|)
block|{
name|super
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetServer
operator|=
name|destinationServer
expr_stmt|;
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
name|REGION_ASSIGN
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|RegionTransitionState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|REGION_TRANSITION_QUEUE
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
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
specifier|final
name|AssignRegionStateData
operator|.
name|Builder
name|state
init|=
name|AssignRegionStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTransitionState
argument_list|(
name|getTransitionState
argument_list|()
argument_list|)
operator|.
name|setRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|forceNewPlan
condition|)
block|{
name|state
operator|.
name|setForceNewPlan
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|targetServer
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|setTargetServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|this
operator|.
name|targetServer
argument_list|)
argument_list|)
expr_stmt|;
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
specifier|final
name|AssignRegionStateData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|AssignRegionStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|setTransitionState
argument_list|(
name|state
operator|.
name|getTransitionState
argument_list|()
argument_list|)
expr_stmt|;
name|setRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|state
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|forceNewPlan
operator|=
name|state
operator|.
name|getForceNewPlan
argument_list|()
expr_stmt|;
if|if
condition|(
name|state
operator|.
name|hasTargetServer
argument_list|()
condition|)
block|{
name|this
operator|.
name|targetServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|state
operator|.
name|getTargetServer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|startTransition
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If the region is already open we can't do much...
if|if
condition|(
name|regionNode
operator|.
name|isInState
argument_list|(
name|State
operator|.
name|OPEN
argument_list|)
operator|&&
name|isServerOnline
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Assigned, not reassigning; "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Don't assign if table is in disabling or disabled state.
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
name|TableName
name|tn
init|=
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|tsm
operator|.
name|isTableState
argument_list|(
name|tn
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|DISABLING
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tn
operator|+
literal|" state="
operator|+
name|tsm
operator|.
name|getTableState
argument_list|(
name|tn
argument_list|)
operator|+
literal|", skipping "
operator|+
name|this
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// If the region is SPLIT, we can't assign it. But state might be CLOSED, rather than
comment|// SPLIT which is what a region gets set to when unassigned as part of SPLIT. FIX.
if|if
condition|(
name|regionNode
operator|.
name|isInState
argument_list|(
name|State
operator|.
name|SPLIT
argument_list|)
operator|||
operator|(
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isOffline
argument_list|()
operator|&&
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isSplit
argument_list|()
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"SPLIT, cannot be assigned; "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|+
literal|"; hri="
operator|+
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// If we haven't started the operation yet, we can abort
if|if
condition|(
name|aborted
operator|.
name|get
argument_list|()
operator|&&
name|regionNode
operator|.
name|isInState
argument_list|(
name|State
operator|.
name|CLOSED
argument_list|,
name|State
operator|.
name|OFFLINE
argument_list|)
condition|)
block|{
if|if
condition|(
name|incrementAndCheckMaxAttempts
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
name|regionNode
operator|.
name|setState
argument_list|(
name|State
operator|.
name|FAILED_OPEN
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
operator|new
name|RetriesExhaustedException
argument_list|(
literal|"Max attempts exceeded"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setAbortFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|"Abort requested"
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
comment|// Send assign (add into assign-pool). Region is now in OFFLINE state. Setting offline state
comment|// scrubs what was the old region location. Setting a new regionLocation here is how we retain
comment|// old assignment or specify target server if a move or merge. See
comment|// AssignmentManager#processAssignQueue. Otherwise, balancer gives us location.
name|ServerName
name|lastRegionLocation
init|=
name|regionNode
operator|.
name|offline
argument_list|()
decl_stmt|;
name|boolean
name|retain
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|forceNewPlan
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|targetServer
operator|!=
literal|null
condition|)
block|{
name|retain
operator|=
name|targetServer
operator|.
name|equals
argument_list|(
name|lastRegionLocation
argument_list|)
expr_stmt|;
name|regionNode
operator|.
name|setRegionLocation
argument_list|(
name|targetServer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|lastRegionLocation
operator|!=
literal|null
condition|)
block|{
comment|// Try and keep the location we had before we offlined.
name|retain
operator|=
literal|true
expr_stmt|;
name|regionNode
operator|.
name|setRegionLocation
argument_list|(
name|lastRegionLocation
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|regionNode
operator|.
name|getLastHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|retain
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting lastHost as the region location "
operator|+
name|regionNode
operator|.
name|getLastHost
argument_list|()
argument_list|)
expr_stmt|;
name|regionNode
operator|.
name|setRegionLocation
argument_list|(
name|regionNode
operator|.
name|getLastHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
operator|+
literal|"; forceNewPlan="
operator|+
name|this
operator|.
name|forceNewPlan
operator|+
literal|", retain="
operator|+
name|retain
argument_list|)
expr_stmt|;
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|queueAssign
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|updateTransition
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|ProcedureSuspendedException
block|{
comment|// TODO: crash if destinationServer is specified and not online
comment|// which is also the case when the balancer provided us with a different location.
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
literal|"Update "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_QUEUE
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
name|isServerOnline
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
comment|// TODO: is this correct? should we wait the chore/ssh?
name|LOG
operator|.
name|info
argument_list|(
literal|"Server not online, re-queuing "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_QUEUE
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|waitServerReportEvent
argument_list|(
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|,
name|this
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Early suspend! "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
if|if
condition|(
name|regionNode
operator|.
name|isInState
argument_list|(
name|State
operator|.
name|OPEN
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Already assigned: "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Transition regionNode State. Set it to OPENING. Update hbase:meta, and add
comment|// region to list of regions on the target regionserver. Need to UNDO if failure!
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|markRegionAsOpening
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
comment|// TODO: Requires a migration to be open by the RS?
comment|// regionNode.getFormatVersion()
if|if
condition|(
operator|!
name|addToRemoteDispatcher
argument_list|(
name|env
argument_list|,
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
condition|)
block|{
comment|// Failed the dispatch BUT addToRemoteDispatcher internally does
comment|// cleanup on failure -- even the undoing of markRegionAsOpening above --
comment|// so nothing more to do here; in fact we need to get out of here
comment|// fast since we've been put back on the scheduler.
block|}
comment|// We always return true, even if we fail dispatch because addToRemoteDispatcher
comment|// failure processing sets state back to REGION_TRANSITION_QUEUE so we try again;
comment|// i.e. return true to keep the Procedure running; it has been reset to startover.
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finishTransition
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
block|{
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|markRegionAsOpened
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
comment|// This success may have been after we failed open a few times. Be sure to cleanup any
comment|// failed open references. See #incrementAndCheckMaxAttempts and where it is called.
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|removeFromFailedOpen
argument_list|(
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reportTransition
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|,
specifier|final
name|TransitionCode
name|code
parameter_list|,
specifier|final
name|long
name|openSeqNum
parameter_list|)
throws|throws
name|UnexpectedStateException
block|{
switch|switch
condition|(
name|code
condition|)
block|{
case|case
name|OPENED
case|:
if|if
condition|(
name|openSeqNum
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Received report unexpected "
operator|+
name|code
operator|+
literal|" transition openSeqNum="
operator|+
name|openSeqNum
operator|+
literal|", "
operator|+
name|regionNode
argument_list|)
throw|;
block|}
if|if
condition|(
name|openSeqNum
operator|<
name|regionNode
operator|.
name|getOpenSeqNum
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skipping update of open seqnum with "
operator|+
name|openSeqNum
operator|+
literal|" because current seqnum="
operator|+
name|regionNode
operator|.
name|getOpenSeqNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|regionNode
operator|.
name|setOpenSeqNum
argument_list|(
name|openSeqNum
argument_list|)
expr_stmt|;
comment|// Leave the state here as OPENING for now. We set it to OPEN in
comment|// REGION_TRANSITION_FINISH section where we do a bunch of checks.
comment|// regionNode.setState(RegionState.State.OPEN, RegionState.State.OPENING);
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_FINISH
argument_list|)
expr_stmt|;
break|break;
case|case
name|FAILED_OPEN
case|:
name|handleFailure
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Received report unexpected "
operator|+
name|code
operator|+
literal|" transition openSeqNum="
operator|+
name|openSeqNum
operator|+
literal|", "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
operator|+
literal|", "
operator|+
name|this
operator|+
literal|", expected OPENED or FAILED_OPEN."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Called when dispatch or subsequent OPEN request fail. Can be run by the    * inline dispatch call or later by the ServerCrashProcedure. Our state is    * generally OPENING. Cleanup and reset to OFFLINE and put our Procedure    * State back to REGION_TRANSITION_QUEUE so the Assign starts over.    */
specifier|private
name|void
name|handleFailure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|)
block|{
if|if
condition|(
name|incrementAndCheckMaxAttempts
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|forceNewPlan
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|targetServer
operator|=
literal|null
expr_stmt|;
name|regionNode
operator|.
name|offline
argument_list|()
expr_stmt|;
comment|// We were moved to OPENING state before dispatch. Undo. It is safe to call
comment|// this method because it checks for OPENING first.
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|undoRegionAsOpening
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_QUEUE
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|incrementAndCheckMaxAttempts
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|)
block|{
specifier|final
name|int
name|retries
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|addToFailedOpen
argument_list|(
name|regionNode
argument_list|)
operator|.
name|incrementAndGetRetries
argument_list|()
decl_stmt|;
name|int
name|max
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getAssignMaxAttempts
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Retry="
operator|+
name|retries
operator|+
literal|" of max="
operator|+
name|max
operator|+
literal|"; "
operator|+
name|this
operator|+
literal|"; "
operator|+
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|retries
operator|>=
name|max
return|;
block|}
annotation|@
name|Override
specifier|public
name|RemoteOperation
name|remoteCallBuild
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
assert|assert
name|serverName
operator|.
name|equals
argument_list|(
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
assert|;
return|return
operator|new
name|RegionOpenOperation
argument_list|(
name|this
argument_list|,
name|getRegionInfo
argument_list|()
argument_list|,
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getFavoredNodes
argument_list|(
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|remoteCallFailed
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionStateNode
name|regionNode
parameter_list|,
specifier|final
name|IOException
name|exception
parameter_list|)
block|{
name|handleFailure
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
name|super
operator|.
name|toStringClassDetails
argument_list|(
name|sb
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|targetServer
operator|!=
literal|null
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|", target="
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|targetServer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServer
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|RegionStateNode
name|node
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStateNode
argument_list|(
name|this
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|node
operator|.
name|getRegionLocation
argument_list|()
return|;
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
name|getAssignmentManager
argument_list|()
operator|.
name|getAssignmentManagerMetrics
argument_list|()
operator|.
name|getAssignProcMetrics
argument_list|()
return|;
block|}
comment|/**    * Sort AssignProcedures such that meta and system assigns come first before user-space assigns.    * Have to do it this way w/ distinct Comparator because Procedure is already Comparable on    * 'Env'(?).    */
specifier|public
specifier|static
class|class
name|CompareAssignProcedure
implements|implements
name|Comparator
argument_list|<
name|AssignProcedure
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|AssignProcedure
name|left
parameter_list|,
name|AssignProcedure
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
if|if
condition|(
name|right
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
return|return
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|left
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|right
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|right
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
return|return
operator|+
literal|1
return|;
block|}
if|if
condition|(
name|left
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
if|if
condition|(
name|right
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
return|return
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|left
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|right
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|right
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
return|return
operator|+
literal|1
return|;
block|}
return|return
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|left
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|right
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

