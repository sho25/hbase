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
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|OpenRegionProcedureStateData
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
comment|/**  * The remote procedure used to open a region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OpenRegionProcedure
extends|extends
name|RegionRemoteProcedureBase
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
name|OpenRegionProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|OpenRegionProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|OpenRegionProcedure
parameter_list|(
name|TransitRegionStateProcedure
name|parent
parameter_list|,
name|RegionInfo
name|region
parameter_list|,
name|ServerName
name|targetServer
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|,
name|region
argument_list|,
name|targetServer
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
name|REGION_ASSIGN
return|;
block|}
annotation|@
name|Override
specifier|public
name|RemoteOperation
name|remoteCallBuild
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ServerName
name|remote
parameter_list|)
block|{
return|return
operator|new
name|RegionOpenOperation
argument_list|(
name|this
argument_list|,
name|region
argument_list|,
name|getProcId
argument_list|()
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
name|OpenRegionProcedureStateData
operator|.
name|getDefaultInstance
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
name|serializer
operator|.
name|deserialize
argument_list|(
name|OpenRegionProcedureStateData
operator|.
name|class
argument_list|)
expr_stmt|;
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
name|getOpenProcMetrics
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reportTransition
parameter_list|(
name|RegionStateNode
name|regionNode
parameter_list|,
name|TransitionCode
name|transitionCode
parameter_list|,
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|transitionCode
condition|)
block|{
case|case
name|OPENED
case|:
comment|// this is the openSeqNum
if|if
condition|(
name|seqId
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
name|TransitionCode
operator|.
name|OPENED
operator|+
literal|" transition openSeqNum="
operator|+
name|seqId
operator|+
literal|", "
operator|+
name|regionNode
operator|+
literal|", proc="
operator|+
name|this
argument_list|)
throw|;
block|}
break|break;
case|case
name|FAILED_OPEN
case|:
comment|// nothing to check
break|break;
default|default:
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Received report unexpected "
operator|+
name|transitionCode
operator|+
literal|" transition, "
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
annotation|@
name|Override
specifier|protected
name|void
name|updateTransition
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|RegionStateNode
name|regionNode
parameter_list|,
name|TransitionCode
name|transitionCode
parameter_list|,
name|long
name|openSeqNum
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|transitionCode
condition|)
block|{
case|case
name|OPENED
case|:
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
literal|"Received report {} transition from {} for {}, pid={} but the new openSeqNum {}"
operator|+
literal|" is less than the current one {}, ignoring..."
argument_list|,
name|transitionCode
argument_list|,
name|targetServer
argument_list|,
name|regionNode
argument_list|,
name|getProcId
argument_list|()
argument_list|,
name|openSeqNum
argument_list|,
name|regionNode
operator|.
name|getOpenSeqNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionNode
operator|.
name|setOpenSeqNum
argument_list|(
name|openSeqNum
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|regionOpened
argument_list|(
name|regionNode
argument_list|)
expr_stmt|;
break|break;
case|case
name|FAILED_OPEN
case|:
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|regionFailedOpen
argument_list|(
name|regionNode
argument_list|,
literal|false
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnexpectedStateException
argument_list|(
literal|"Unexpected transition code: "
operator|+
name|transitionCode
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

