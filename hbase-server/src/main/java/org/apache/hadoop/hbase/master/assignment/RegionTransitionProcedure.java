begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|TableProcedureInterface
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
name|FailedRemoteDispatchException
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
name|Procedure
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
name|procedure2
operator|.
name|RemoteProcedureException
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
name|ProcedureProtos
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

begin_comment
comment|/**  * Base class for the Assign and Unassign Procedure.  *  * Locking:  * Takes exclusive lock on the region being assigned/unassigned. Thus, there can only be one  * RegionTransitionProcedure per region running at a time (see MasterProcedureScheduler).  *  *<p>This procedure is asynchronous and responds to external events.  * The AssignmentManager will notify this procedure when the RS completes  * the operation and reports the transitioned state  * (see the Assign and Unassign class for more detail).</p>  *  *<p>Procedures move from the REGION_TRANSITION_QUEUE state when they are  * first submitted, to the REGION_TRANSITION_DISPATCH state when the request  * to remote server is sent and the Procedure is suspended waiting on external  * event to be woken again. Once the external event is triggered, Procedure  * moves to the REGION_TRANSITION_FINISH state.</p>  *  *<p>NOTE: {@link AssignProcedure} and {@link UnassignProcedure} should not be thought of  * as being asymmetric, at least currently.  *<ul>  *<li>{@link AssignProcedure} moves through all the above described states and implements methods  * associated with each while {@link UnassignProcedure} starts at state  * REGION_TRANSITION_DISPATCH and state REGION_TRANSITION_QUEUE is not supported.</li>  *  *<li>When any step in {@link AssignProcedure} fails, failure handler  * AssignProcedure#handleFailure(MasterProcedureEnv, RegionStateNode) re-attempts the  * assignment by setting the procedure state to REGION_TRANSITION_QUEUE and forces  * assignment to a different target server by setting {@link AssignProcedure#forceNewPlan}. When  * the number of attempts reaches threshold configuration 'hbase.assignment.maximum.attempts',  * the procedure is aborted. For {@link UnassignProcedure}, similar re-attempts are  * intentionally not implemented. It is a 'one shot' procedure. See its class doc for how it  * handles failure.  *</li>  *<li>If we find a region in an 'unexpected' state, we'll complain and retry with backoff forever.  * The 'unexpected' state needs to be fixed either by another running Procedure or by operator  * intervention (Regions in 'unexpected' state indicates bug or unexpected transition type).  * For this to work, subclasses need to persist the 'attempt' counter kept in this class when  * they do serializeStateData and restore it inside their deserializeStateData, just as they do  * for {@link #regionInfo}.  *</li>  *</ul>  *</p>  *  *<p>TODO: Considering it is a priority doing all we can to get make a region available as soon as  * possible, re-attempting with any target makes sense if specified target fails in case of  * {@link AssignProcedure}. For {@link UnassignProcedure}, our concern is preventing data loss  * on failed unassign. See class doc for explanation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|RegionTransitionProcedure
extends|extends
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
implements|implements
name|TableProcedureInterface
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
name|RegionTransitionProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|RegionTransitionState
name|transitionState
init|=
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_QUEUE
decl_stmt|;
comment|/**    * This data member must be persisted. Expectation is that it is done by subclasses in their    * {@link #serializeStateData(ProcedureStateSerializer)} call, restoring {@link #regionInfo}    * in their {@link #deserializeStateData(ProcedureStateSerializer)} method.    */
specifier|private
name|RegionInfo
name|regionInfo
decl_stmt|;
comment|/**    * Like {@link #regionInfo}, the expectation is that subclasses persist the value of this    * data member. It is used doing backoff when Procedure gets stuck.    */
specifier|private
name|int
name|attempt
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|lock
init|=
literal|false
decl_stmt|;
comment|// Required by the Procedure framework to create the procedure on replay
specifier|public
name|RegionTransitionProcedure
parameter_list|()
block|{}
specifier|public
name|RegionTransitionProcedure
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|RegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|regionInfo
return|;
block|}
comment|/**    * This setter is for subclasses to call in their    * {@link #deserializeStateData(ProcedureStateSerializer)} method. Expectation is that    * subclasses will persist `regioninfo` in their    * {@link #serializeStateData(ProcedureStateSerializer)} method and then restore `regionInfo` on    * deserialization by calling.    */
specifier|protected
name|void
name|setRegionInfo
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
block|}
comment|/**    * This setter is for subclasses to call in their    * {@link #deserializeStateData(ProcedureStateSerializer)} method.    * @see #setRegionInfo(RegionInfo)    */
specifier|protected
name|void
name|setAttempt
parameter_list|(
name|int
name|attempt
parameter_list|)
block|{
name|this
operator|.
name|attempt
operator|=
name|attempt
expr_stmt|;
block|}
specifier|protected
name|int
name|getAttempt
parameter_list|()
block|{
return|return
name|this
operator|.
name|attempt
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
name|RegionInfo
name|hri
init|=
name|getRegionInfo
argument_list|()
decl_stmt|;
return|return
name|hri
operator|!=
literal|null
condition|?
name|hri
operator|.
name|getTable
argument_list|()
else|:
literal|null
return|;
block|}
specifier|public
name|boolean
name|isMeta
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|getTableName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|toStringClassDetails
parameter_list|(
specifier|final
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
literal|" table="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", region="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getRegionInfo
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RegionStateNode
name|getRegionState
parameter_list|(
specifier|final
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
name|getRegionStates
argument_list|()
operator|.
name|getOrCreateRegionStateNode
argument_list|(
name|getRegionInfo
argument_list|()
argument_list|)
return|;
block|}
name|void
name|setTransitionState
parameter_list|(
specifier|final
name|RegionTransitionState
name|state
parameter_list|)
block|{
name|this
operator|.
name|transitionState
operator|=
name|state
expr_stmt|;
block|}
name|RegionTransitionState
name|getTransitionState
parameter_list|()
block|{
return|return
name|transitionState
return|;
block|}
specifier|protected
specifier|abstract
name|boolean
name|startTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|ProcedureSuspendedException
function_decl|;
comment|/**    * Called when the Procedure is in the REGION_TRANSITION_DISPATCH state.    * In here we do the RPC call to OPEN/CLOSE the region. The suspending of    * the thread so it sleeps until it gets update that the OPEN/CLOSE has    * succeeded is complicated. Read the implementations to learn more.    */
specifier|protected
specifier|abstract
name|boolean
name|updateTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|ProcedureSuspendedException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|finishTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|)
throws|throws
name|IOException
throws|,
name|ProcedureSuspendedException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|reportTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|,
name|TransitionCode
name|code
parameter_list|,
name|long
name|seqId
parameter_list|)
throws|throws
name|UnexpectedStateException
function_decl|;
annotation|@
name|Override
specifier|public
specifier|abstract
name|RemoteOperation
name|remoteCallBuild
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
function_decl|;
comment|/**    * @return True if processing of fail is complete; the procedure will be woken from its suspend    * and we'll go back to running through procedure steps:    * otherwise if false we leave the procedure in suspended state.    */
specifier|protected
specifier|abstract
name|boolean
name|remoteCallFailed
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|,
name|IOException
name|exception
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|remoteCallFailed
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
name|IOException
name|exception
parameter_list|)
block|{
specifier|final
name|RegionStateNode
name|regionNode
init|=
name|getRegionState
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Remote call failed {}; {}; {}; exception={}"
argument_list|,
name|serverName
argument_list|,
name|this
argument_list|,
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|,
name|exception
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|remoteCallFailed
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|,
name|exception
argument_list|)
condition|)
block|{
comment|// NOTE: This call to wakeEvent puts this Procedure back on the scheduler.
comment|// Thereafter, another Worker can be in here so DO NOT MESS WITH STATE beyond
comment|// this method. Just get out of this current processing quickly.
name|regionNode
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|wake
argument_list|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// else leave the procedure in suspended state; it is waiting on another call to this callback
block|}
comment|/**    * Be careful! At the end of this method, the procedure has either succeeded    * and this procedure has been set into a suspended state OR, we failed and    * this procedure has been put back on the scheduler ready for another worker    * to pick it up. In both cases, we need to exit the current Worker processing    * immediately!    * @return True if we successfully dispatched the call and false if we failed;    * if failed, we need to roll back any setup done for the dispatch.    */
specifier|protected
name|boolean
name|addToRemoteDispatcher
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|targetServer
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Dispatch {}; {}"
argument_list|,
name|this
argument_list|,
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Put this procedure into suspended mode to wait on report of state change
comment|// from remote regionserver. Means Procedure associated ProcedureEvent is marked not 'ready'.
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|suspend
argument_list|()
expr_stmt|;
comment|// Tricky because the below call to addOperationToNode can fail. If it fails, we need to
comment|// backtrack on stuff like the 'suspend' done above -- tricky as the 'wake' requests us -- and
comment|// ditto up in the caller; it needs to undo state changes. Inside in remoteCallFailed, it does
comment|// wake to undo the above suspend.
try|try
block|{
name|env
operator|.
name|getRemoteDispatcher
argument_list|()
operator|.
name|addOperationToNode
argument_list|(
name|targetServer
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FailedRemoteDispatchException
name|frde
parameter_list|)
block|{
name|remoteCallFailed
argument_list|(
name|env
argument_list|,
name|targetServer
argument_list|,
name|frde
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|protected
name|void
name|reportTransition
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
name|TransitionCode
name|code
parameter_list|,
specifier|final
name|long
name|seqId
parameter_list|)
throws|throws
name|UnexpectedStateException
block|{
specifier|final
name|RegionStateNode
name|regionNode
init|=
name|getRegionState
argument_list|(
name|env
argument_list|)
decl_stmt|;
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
literal|"Received report "
operator|+
name|code
operator|+
literal|" seqId="
operator|+
name|seqId
operator|+
literal|", "
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
operator|!
name|serverName
operator|.
name|equals
argument_list|(
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|isMeta
argument_list|()
operator|&&
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|regionNode
operator|.
name|setRegionLocation
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Unexpected state=%s from server=%s; expected server=%s; %s; %s"
argument_list|,
name|code
argument_list|,
name|serverName
argument_list|,
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|,
name|this
argument_list|,
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|reportTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|,
name|code
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
comment|// NOTE: This call adds this procedure back on the scheduler.
comment|// This makes it so this procedure can run again. Another worker will take
comment|// processing to the next stage. At an extreme, the other worker may run in
comment|// parallel so DO  NOT CHANGE any state hereafter! This should be last thing
comment|// done in this processing step.
name|regionNode
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|wake
argument_list|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|boolean
name|isServerOnline
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
return|return
name|isServerOnline
argument_list|(
name|env
argument_list|,
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|boolean
name|isServerOnline
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
return|return
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|isServerOnline
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|toStringState
parameter_list|(
name|StringBuilder
name|builder
parameter_list|)
block|{
name|super
operator|.
name|toStringState
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|RegionTransitionState
name|ts
init|=
name|this
operator|.
name|transitionState
decl_stmt|;
if|if
condition|(
operator|!
name|isFinished
argument_list|()
operator|&&
name|ts
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureSuspendedException
block|{
specifier|final
name|AssignmentManager
name|am
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
specifier|final
name|RegionStateNode
name|regionNode
init|=
name|getRegionState
argument_list|(
name|env
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|am
operator|.
name|addRegionInTransition
argument_list|(
name|regionNode
argument_list|,
name|this
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
name|String
operator|.
name|format
argument_list|(
literal|"There is already another procedure running on this region this=%s owner=%s"
argument_list|,
name|this
argument_list|,
name|regionNode
operator|.
name|getProcedure
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|msg
operator|+
literal|" "
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
name|setAbortFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|msg
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
try|try
block|{
name|boolean
name|retry
decl_stmt|;
do|do
block|{
name|retry
operator|=
literal|false
expr_stmt|;
switch|switch
condition|(
name|transitionState
condition|)
block|{
case|case
name|REGION_TRANSITION_QUEUE
case|:
comment|// 1. push into the AM queue for balancer policy
if|if
condition|(
operator|!
name|startTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
comment|// The operation figured it is done or it aborted; check getException()
name|am
operator|.
name|removeRegionInTransition
argument_list|(
name|getRegionState
argument_list|(
name|env
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|transitionState
operator|=
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_DISPATCH
expr_stmt|;
if|if
condition|(
name|regionNode
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|suspendIfNotReady
argument_list|(
name|this
argument_list|)
condition|)
block|{
comment|// Why this suspend? Because we want to ensure Store happens before proceed?
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
break|break;
case|case
name|REGION_TRANSITION_DISPATCH
case|:
comment|// 2. send the request to the target server
if|if
condition|(
operator|!
name|updateTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
condition|)
block|{
comment|// The operation figured it is done or it aborted; check getException()
name|am
operator|.
name|removeRegionInTransition
argument_list|(
name|regionNode
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|transitionState
operator|!=
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_DISPATCH
condition|)
block|{
name|retry
operator|=
literal|true
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|regionNode
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|suspendIfNotReady
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
break|break;
case|case
name|REGION_TRANSITION_FINISH
case|:
comment|// 3. wait assignment response. completion/failure
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finishing {}; {}"
argument_list|,
name|this
argument_list|,
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|)
expr_stmt|;
name|finishTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|)
expr_stmt|;
name|am
operator|.
name|removeRegionInTransition
argument_list|(
name|regionNode
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
do|while
condition|(
name|retry
condition|)
do|;
comment|// If here, success so clear out the attempt counter so we start fresh each time we get stuck.
name|this
operator|.
name|attempt
operator|=
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|long
name|backoff
init|=
name|getBackoffTime
argument_list|(
name|this
operator|.
name|attempt
operator|++
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed transition, suspend {}secs {}; {}; waiting on rectified condition fixed "
operator|+
literal|"by other Procedure or operator intervention"
argument_list|,
name|backoff
operator|/
literal|1000
argument_list|,
name|this
argument_list|,
name|regionNode
operator|.
name|toShortString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|suspend
argument_list|()
expr_stmt|;
if|if
condition|(
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|suspendIfNotReady
argument_list|(
name|this
argument_list|)
condition|)
block|{
name|setTimeout
argument_list|(
name|Math
operator|.
name|toIntExact
argument_list|(
name|backoff
argument_list|)
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
block|}
return|return
operator|new
name|Procedure
index|[]
block|{
name|this
block|}
return|;
block|}
specifier|private
name|long
name|getBackoffTime
parameter_list|(
name|int
name|attempts
parameter_list|)
block|{
name|long
name|backoffTime
init|=
call|(
name|long
call|)
argument_list|(
literal|1000
operator|*
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|attempts
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|maxBackoffTime
init|=
literal|60
operator|*
literal|60
operator|*
literal|1000
decl_stmt|;
comment|// An hour. Hard-coded for for now.
return|return
name|backoffTime
operator|<
name|maxBackoffTime
condition|?
name|backoffTime
else|:
name|maxBackoffTime
return|;
block|}
comment|/**    * At end of timeout, wake ourselves up so we run again.    */
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|boolean
name|setTimeoutFailure
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
name|getRegionState
argument_list|(
name|env
argument_list|)
operator|.
name|getProcedureEvent
argument_list|()
operator|.
name|wake
argument_list|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
comment|// 'false' means that this procedure handled the timeout
block|}
annotation|@
name|Override
specifier|protected
name|void
name|rollback
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|isRollbackSupported
argument_list|(
name|transitionState
argument_list|)
condition|)
block|{
comment|// Nothing done up to this point. abort safely.
comment|// This should happen when something like disableTable() is triggered.
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|removeRegionInTransition
argument_list|(
name|getRegionState
argument_list|(
name|env
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// There is no rollback for assignment unless we cancel the operation by
comment|// dropping/disabling the table.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unhandled state "
operator|+
name|transitionState
operator|+
literal|"; there is no rollback for assignment unless we cancel the operation by "
operator|+
literal|"dropping/disabling the table"
argument_list|)
throw|;
block|}
specifier|protected
specifier|abstract
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|RegionTransitionState
name|state
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|protected
name|boolean
name|abort
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|isRollbackSupported
argument_list|(
name|transitionState
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
return|return
literal|true
return|;
block|}
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
comment|// Unless we are assigning meta, wait for meta to be available and loaded.
if|if
condition|(
operator|!
name|isMeta
argument_list|()
operator|&&
operator|(
name|env
operator|.
name|waitFailoverCleanup
argument_list|(
name|this
argument_list|)
operator|||
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|waitMetaInitialized
argument_list|(
name|this
argument_list|,
name|getRegionInfo
argument_list|()
argument_list|)
operator|)
condition|)
block|{
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
comment|// TODO: Revisit this and move it to the executor
if|if
condition|(
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|waitRegion
argument_list|(
name|this
argument_list|,
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|LockState
operator|.
name|LOCK_EVENT_WAIT
operator|+
literal|" pid="
operator|+
name|getProcId
argument_list|()
operator|+
literal|" "
operator|+
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|dumpLocks
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// TODO Auto-generated catch block
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
name|LockState
operator|.
name|LOCK_EVENT_WAIT
return|;
block|}
name|this
operator|.
name|lock
operator|=
literal|true
expr_stmt|;
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
name|wakeRegion
argument_list|(
name|this
argument_list|,
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|lock
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|holdLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
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
name|hasLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|lock
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
comment|/**    * Used by ServerCrashProcedure to see if this Assign/Unassign needs processing.    * @return ServerName the Assign or Unassign is going against.    */
specifier|public
specifier|abstract
name|ServerName
name|getServer
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|remoteOperationCompleted
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// should not be called for region operation until we modified the open/close region procedure
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remoteOperationFailed
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RemoteProcedureException
name|error
parameter_list|)
block|{
comment|// should not be called for region operation until we modified the open/close region procedure
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

