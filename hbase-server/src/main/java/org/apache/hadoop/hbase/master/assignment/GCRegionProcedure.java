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
name|backup
operator|.
name|HFileArchiver
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
name|favored
operator|.
name|FavoredNodesManager
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
name|procedure
operator|.
name|AbstractStateMachineRegionProcedure
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
name|collect
operator|.
name|Lists
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
name|GCRegionState
import|;
end_import

begin_comment
comment|/**  * GC a Region that is no longer in use. It has been split or merged away.  * Caller determines if it is GC time. This Procedure does not check.  *<p>This is a Region StateMachine Procedure. We take a read lock on the Table and then  * exclusive on the Region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|GCRegionProcedure
extends|extends
name|AbstractStateMachineRegionProcedure
argument_list|<
name|GCRegionState
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
name|GCRegionProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|GCRegionProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionInfo
name|hri
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|,
name|hri
argument_list|)
expr_stmt|;
block|}
specifier|public
name|GCRegionProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
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
name|REGION_GC
return|;
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
name|GCRegionState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
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
name|this
operator|+
literal|" execute state="
operator|+
name|state
argument_list|)
expr_stmt|;
block|}
name|MasterServices
name|masterServices
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
decl_stmt|;
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|GC_REGION_PREPARE
case|:
comment|// Nothing to do to prepare.
name|setNextState
argument_list|(
name|GCRegionState
operator|.
name|GC_REGION_ARCHIVE
argument_list|)
expr_stmt|;
break|break;
case|case
name|GC_REGION_ARCHIVE
case|:
name|FileSystem
name|fs
init|=
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
if|if
condition|(
name|HFileArchiver
operator|.
name|exists
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|getRegion
argument_list|()
argument_list|)
condition|)
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
literal|"Archiving region="
operator|+
name|getRegion
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
argument_list|)
expr_stmt|;
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|masterServices
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|setNextState
argument_list|(
name|GCRegionState
operator|.
name|GC_REGION_PURGE_METADATA
argument_list|)
expr_stmt|;
break|break;
case|case
name|GC_REGION_PURGE_METADATA
case|:
comment|// TODO: Purge metadata before removing from HDFS? This ordering is copied
comment|// from CatalogJanitor.
name|AssignmentManager
name|am
init|=
name|masterServices
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|am
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|am
operator|.
name|getRegionStates
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|deleteRegion
argument_list|(
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|MetaTableAccessor
operator|.
name|deleteRegion
argument_list|(
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
name|masterServices
operator|.
name|getServerManager
argument_list|()
operator|.
name|removeRegion
argument_list|(
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
name|FavoredNodesManager
name|fnm
init|=
name|masterServices
operator|.
name|getFavoredNodesManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|fnm
operator|!=
literal|null
condition|)
block|{
name|fnm
operator|.
name|deleteFavoredNodesForRegions
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|getRegion
argument_list|()
argument_list|)
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
name|this
operator|+
literal|" unhandled state="
operator|+
name|state
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// TODO: This is going to spew log?
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error trying to GC "
operator|+
name|getRegion
argument_list|()
operator|.
name|getShortNameToLog
argument_list|()
operator|+
literal|"; retrying..."
argument_list|,
name|ioe
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
name|GCRegionState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// no-op
block|}
annotation|@
name|Override
specifier|protected
name|GCRegionState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|GCRegionState
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
name|GCRegionState
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
name|GCRegionState
name|getInitialState
parameter_list|()
block|{
return|return
name|GCRegionState
operator|.
name|GC_REGION_PREPARE
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
comment|// Double serialization of regionname. Superclass is also serializing. Fix.
specifier|final
name|MasterProcedureProtos
operator|.
name|GCRegionStateData
operator|.
name|Builder
name|msg
init|=
name|MasterProcedureProtos
operator|.
name|GCRegionStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|getRegion
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|msg
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
specifier|final
name|MasterProcedureProtos
operator|.
name|GCRegionStateData
name|msg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|GCRegionStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|setRegion
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|msg
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
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
operator|.
name|LockState
name|acquireLock
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|super
operator|.
name|acquireLock
argument_list|(
name|env
argument_list|)
return|;
block|}
block|}
end_class

end_unit

