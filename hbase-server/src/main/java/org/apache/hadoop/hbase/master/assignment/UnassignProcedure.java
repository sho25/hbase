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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
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
name|NotServingRegionException
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
name|ipc
operator|.
name|ServerNotRunningYetException
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
name|RegionCloseOperation
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
name|ServerCrashException
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
name|regionserver
operator|.
name|RegionServerAbortedException
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
name|regionserver
operator|.
name|RegionServerStoppedException
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
name|MasterProcedureProtos
operator|.
name|UnassignRegionStateData
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
comment|/**  * Procedure that describes the unassignment of a single region.  * There can only be one RegionTransitionProcedure -- i.e. an assign or an unassign -- per region  * running at a time, since each procedure takes a lock on the region.  *  *<p>The Unassign starts by placing a "close region" request in the Remote Dispatcher  * queue, and the procedure will then go into a "waiting state" (suspend).  * The Remote Dispatcher will batch the various requests for that server and  * they will be sent to the RS for execution.  * The RS will complete the open operation by calling master.reportRegionStateTransition().  * The AM will intercept the transition report, and notify this procedure.  * The procedure will wakeup and finish the unassign by publishing its new state on meta.  *<p>If we are unable to contact the remote regionserver whether because of ConnectException  * or socket timeout, we will call expire on the server we were trying to contact. We will remain  * in suspended state waiting for a wake up from the ServerCrashProcedure that is processing the  * failed server. The basic idea is that if we notice a crashed server, then we have a  * responsibility; i.e. we should not let go of the region until we are sure the server that was  * hosting has had its crash processed. If we let go of the region before then, an assign might  * run before the logs have been split which would make for data loss.  *  *<p>TODO: Rather than this tricky coordination between SCP and this Procedure, instead, work on  * returning a SCP as our subprocedure; probably needs work on the framework to do this,  * especially if the SCP already created.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UnassignProcedure
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
name|UnassignProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Where to send the unassign RPC.    */
specifier|protected
specifier|volatile
name|ServerName
name|hostingServer
decl_stmt|;
comment|/**    * The Server we will subsequently assign the region too (can be null).    */
specifier|protected
specifier|volatile
name|ServerName
name|destinationServer
decl_stmt|;
comment|// TODO: should this be in a reassign procedure?
comment|//       ...and keep unassign for 'disable' case?
specifier|private
name|boolean
name|force
decl_stmt|;
specifier|public
name|UnassignProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|UnassignProcedure
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|ServerName
name|hostingServer
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
block|{
name|this
argument_list|(
name|regionInfo
argument_list|,
name|hostingServer
argument_list|,
literal|null
argument_list|,
name|force
argument_list|)
expr_stmt|;
block|}
specifier|public
name|UnassignProcedure
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|ServerName
name|hostingServer
parameter_list|,
specifier|final
name|ServerName
name|destinationServer
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
block|{
name|super
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostingServer
operator|=
name|hostingServer
expr_stmt|;
name|this
operator|.
name|destinationServer
operator|=
name|destinationServer
expr_stmt|;
name|this
operator|.
name|force
operator|=
name|force
expr_stmt|;
comment|// we don't need REGION_TRANSITION_QUEUE, we jump directly to sending the request
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_DISPATCH
argument_list|)
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
name|REGION_UNASSIGN
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
case|case
name|REGION_TRANSITION_DISPATCH
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
name|UnassignRegionStateData
operator|.
name|Builder
name|state
init|=
name|UnassignRegionStateData
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
name|setHostingServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|this
operator|.
name|hostingServer
argument_list|)
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
name|this
operator|.
name|destinationServer
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|setDestinationServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|destinationServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|force
condition|)
block|{
name|state
operator|.
name|setForce
argument_list|(
literal|true
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
name|UnassignRegionStateData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|UnassignRegionStateData
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
name|this
operator|.
name|hostingServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|state
operator|.
name|getHostingServer
argument_list|()
argument_list|)
expr_stmt|;
name|force
operator|=
name|state
operator|.
name|getForce
argument_list|()
expr_stmt|;
if|if
condition|(
name|state
operator|.
name|hasDestinationServer
argument_list|()
condition|)
block|{
name|this
operator|.
name|destinationServer
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|state
operator|.
name|getDestinationServer
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
block|{
comment|// nothing to do here. we skip the step in the constructor
comment|// by jumping to REGION_TRANSITION_DISPATCH
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
block|{
comment|// if the region is already closed or offline we can't do much...
if|if
condition|(
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Not unassigned "
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
comment|// if we haven't started the operation yet, we can abort
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
name|OPEN
argument_list|)
condition|)
block|{
name|setAbortFailure
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|"abort requested"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Mark the region as CLOSING.
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|markRegionAsClosing
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
comment|// Add the close region operation the the server dispatch queue.
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
comment|// If addToRemoteDispatcher fails, it calls the callback #remoteCallFailed.
block|}
comment|// Return true to keep the procedure running.
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
name|markRegionAsClosed
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
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
name|RegionCloseOperation
argument_list|(
name|this
argument_list|,
name|getRegionInfo
argument_list|()
argument_list|,
name|this
operator|.
name|destinationServer
argument_list|)
return|;
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
name|seqId
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
name|CLOSED
case|:
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_FINISH
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Received report unexpected transition state=%s for region=%s server=%s, expected CLOSED."
argument_list|,
name|code
argument_list|,
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
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
comment|// TODO: Is there on-going rpc to cleanup?
if|if
condition|(
name|exception
operator|instanceof
name|ServerCrashException
condition|)
block|{
comment|// This exception comes from ServerCrashProcedure after log splitting.
comment|// SCP found this region as a RIT. Its call into here says it is ok to let this procedure go
comment|// on to a complete close now. This will release lock on this region so subsequent action on
comment|// region can succeed; e.g. the assign that follows this unassign when a move (w/o wait on SCP
comment|// the assign could run w/o logs being split so data loss).
try|try
block|{
name|reportTransition
argument_list|(
name|env
argument_list|,
name|regionNode
argument_list|,
name|TransitionCode
operator|.
name|CLOSED
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnexpectedStateException
name|e
parameter_list|)
block|{
comment|// Should never happen.
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|RegionServerAbortedException
operator|||
name|exception
operator|instanceof
name|RegionServerStoppedException
operator|||
name|exception
operator|instanceof
name|ServerNotRunningYetException
condition|)
block|{
comment|// TODO
comment|// RS is aborting, we cannot offline the region since the region may need to do WAL
comment|// recovery. Until we see the RS expiration, we should retry.
comment|// TODO: This should be suspend like the below where we call expire on server?
name|LOG
operator|.
name|info
argument_list|(
literal|"Ignoring; waiting on ServerCrashProcedure"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|exception
operator|instanceof
name|NotServingRegionException
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"IS THIS OK? ANY LOGS TO REPLAY; ACTING AS THOUGH ALL GOOD "
operator|+
name|regionNode
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|setTransitionState
argument_list|(
name|RegionTransitionState
operator|.
name|REGION_TRANSITION_FINISH
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Expiring server "
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
literal|", exception="
operator|+
name|exception
argument_list|)
expr_stmt|;
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|expireServer
argument_list|(
name|regionNode
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
expr_stmt|;
comment|// Return false so this procedure stays in suspended state. It will be woken up by a
comment|// ServerCrashProcedure when it notices this RIT.
comment|// TODO: Add a SCP as a new subprocedure that we now come to depend on.
return|return
literal|false
return|;
block|}
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
name|sb
operator|.
name|append
argument_list|(
literal|", server="
argument_list|)
operator|.
name|append
argument_list|(
name|this
operator|.
name|hostingServer
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
return|return
name|this
operator|.
name|hostingServer
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
name|getUnassignProcMetrics
argument_list|()
return|;
block|}
block|}
end_class

end_unit

