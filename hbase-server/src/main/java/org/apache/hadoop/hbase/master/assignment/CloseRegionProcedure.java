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
name|master
operator|.
name|RegionState
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
name|CloseRegionProcedureStateData
import|;
end_import

begin_comment
comment|/**  * The remote procedure used to close a region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CloseRegionProcedure
extends|extends
name|RegionRemoteProcedureBase
block|{
comment|// For a region move operation, we will assign the region after we unassign it, this is the target
comment|// server for the subsequent assign. We will send this value to RS, and RS will record the region
comment|// in a Map to tell client that where the region has been moved to. Can be null. And also, can be
comment|// wrong(but do not make it wrong intentionally). The client can handle this error.
specifier|private
name|ServerName
name|assignCandidate
decl_stmt|;
specifier|public
name|CloseRegionProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CloseRegionProcedure
parameter_list|(
name|RegionInfo
name|region
parameter_list|,
name|ServerName
name|targetServer
parameter_list|,
name|ServerName
name|assignCandidate
parameter_list|)
block|{
name|super
argument_list|(
name|region
argument_list|,
name|targetServer
argument_list|)
expr_stmt|;
name|this
operator|.
name|assignCandidate
operator|=
name|assignCandidate
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
name|RegionCloseOperation
argument_list|(
name|this
argument_list|,
name|region
argument_list|,
name|assignCandidate
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
name|CloseRegionProcedureStateData
operator|.
name|Builder
name|builder
init|=
name|CloseRegionProcedureStateData
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|assignCandidate
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setAssignCandidate
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|assignCandidate
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
name|CloseRegionProcedureStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|CloseRegionProcedureStateData
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|hasAssignCandidate
argument_list|()
condition|)
block|{
name|assignCandidate
operator|=
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|data
operator|.
name|getAssignCandidate
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|shouldDispatch
parameter_list|(
name|RegionStateNode
name|regionNode
parameter_list|)
block|{
return|return
name|regionNode
operator|.
name|isInState
argument_list|(
name|RegionState
operator|.
name|State
operator|.
name|CLOSING
argument_list|)
return|;
block|}
block|}
end_class

end_unit

