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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|procedure
operator|.
name|AbstractStateMachineTableProcedure
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
name|GCMergedRegionsState
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
name|GCMultipleMergedRegionsStateData
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
comment|/**  * GC regions that have been Merged. Caller determines if it is GC time. This Procedure does not  * check. This is a Table Procedure. We take a read lock on the Table. We do NOT keep a lock for  * the life of this procedure. The sub-procedures take locks on the Regions they are purging.  * Replaces a Procedure that did two regions only at a time instead doing multiple merges in the  * one go; only difference from the old {@link GCMergedRegionsState} is the serialization; this  * class has a different serialization profile writing out more than just two regions.  */
end_comment

begin_class
annotation|@
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|GCMultipleMergedRegionsProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|GCMergedRegionsState
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
name|GCMultipleMergedRegionsProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|parents
decl_stmt|;
specifier|private
name|RegionInfo
name|mergedChild
decl_stmt|;
specifier|public
name|GCMultipleMergedRegionsProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|RegionInfo
name|mergedChild
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|parents
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|this
operator|.
name|parents
operator|=
name|parents
expr_stmt|;
name|this
operator|.
name|mergedChild
operator|=
name|mergedChild
expr_stmt|;
block|}
specifier|public
name|GCMultipleMergedRegionsProcedure
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
name|MERGED_REGIONS_GC
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
name|GCMergedRegionsState
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
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|GC_MERGED_REGIONS_PREPARE
case|:
comment|// Nothing to do to prepare.
name|setNextState
argument_list|(
name|GCMergedRegionsState
operator|.
name|GC_MERGED_REGIONS_PURGE
argument_list|)
expr_stmt|;
break|break;
case|case
name|GC_MERGED_REGIONS_PURGE
case|:
name|addChildProcedure
argument_list|(
name|createGCRegionProcedures
argument_list|(
name|env
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|GCMergedRegionsState
operator|.
name|GC_REGION_EDIT_METADATA
argument_list|)
expr_stmt|;
break|break;
case|case
name|GC_REGION_EDIT_METADATA
case|:
name|MetaTableAccessor
operator|.
name|deleteMergeQualifiers
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|mergedChild
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
literal|"Error trying to GC merged regions {}; retrying..."
argument_list|,
name|this
operator|.
name|parents
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|RegionInfo
operator|.
name|getShortNameToLog
argument_list|(
name|r
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|", "
argument_list|)
argument_list|)
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
specifier|private
name|GCRegionProcedure
index|[]
name|createGCRegionProcedures
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|GCRegionProcedure
index|[]
name|procs
init|=
operator|new
name|GCRegionProcedure
index|[
name|this
operator|.
name|parents
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|this
operator|.
name|parents
control|)
block|{
name|GCRegionProcedure
name|proc
init|=
operator|new
name|GCRegionProcedure
argument_list|(
name|env
argument_list|,
name|ri
argument_list|)
decl_stmt|;
name|proc
operator|.
name|setOwner
argument_list|(
name|env
operator|.
name|getRequestUser
argument_list|()
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
name|procs
index|[
name|index
operator|++
index|]
operator|=
name|proc
expr_stmt|;
block|}
return|return
name|procs
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
name|GCMergedRegionsState
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
name|GCMergedRegionsState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|GCMergedRegionsState
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
name|GCMergedRegionsState
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
name|GCMergedRegionsState
name|getInitialState
parameter_list|()
block|{
return|return
name|GCMergedRegionsState
operator|.
name|GC_MERGED_REGIONS_PREPARE
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
specifier|final
name|GCMultipleMergedRegionsStateData
operator|.
name|Builder
name|msg
init|=
name|GCMultipleMergedRegionsStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|addAllParents
argument_list|(
name|this
operator|.
name|parents
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toRegionInfo
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setMergedChild
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|this
operator|.
name|mergedChild
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
name|GCMultipleMergedRegionsStateData
name|msg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|GCMultipleMergedRegionsStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|parents
operator|=
name|msg
operator|.
name|getParentsList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toRegionInfo
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|mergedChild
operator|=
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|msg
operator|.
name|getMergedChild
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
literal|" child="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|mergedChild
operator|.
name|getShortNameToLog
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", parents:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|parents
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|RegionInfo
operator|.
name|getShortNameToLog
argument_list|(
name|r
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|", "
argument_list|)
argument_list|)
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
name|this
operator|.
name|mergedChild
operator|.
name|getTable
argument_list|()
return|;
block|}
block|}
end_class

end_unit

