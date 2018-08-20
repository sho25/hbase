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
name|Collections
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
name|HRegionLocation
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
name|master
operator|.
name|assignment
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
name|assignment
operator|.
name|TransitRegionStateProcedure
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
name|ReopenTableRegionsState
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
name|ReopenTableRegionsStateData
import|;
end_import

begin_comment
comment|/**  * Used for reopening the regions for a table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ReopenTableRegionsProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|ReopenTableRegionsState
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
name|ReopenTableRegionsProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regions
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|public
name|ReopenTableRegionsProcedure
parameter_list|()
block|{   }
specifier|public
name|ReopenTableRegionsProcedure
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
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
name|tableName
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
name|REGION_EDIT
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
name|ReopenTableRegionsState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|REOPEN_TABLE_REGIONS_GET_REGIONS
case|:
if|if
condition|(
operator|!
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table {} is disabled, give up reopening its regions"
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
name|regions
operator|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTableForReopen
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ReopenTableRegionsState
operator|.
name|REOPEN_TABLE_REGIONS_REOPEN_REGIONS
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REOPEN_TABLE_REGIONS_REOPEN_REGIONS
case|:
for|for
control|(
name|HRegionLocation
name|loc
range|:
name|regions
control|)
block|{
name|RegionStateNode
name|regionNode
init|=
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
name|loc
operator|.
name|getRegion
argument_list|()
argument_list|)
decl_stmt|;
name|TransitRegionStateProcedure
name|proc
decl_stmt|;
name|regionNode
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|regionNode
operator|.
name|getProcedure
argument_list|()
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
name|proc
operator|=
name|TransitRegionStateProcedure
operator|.
name|reopen
argument_list|(
name|env
argument_list|,
name|regionNode
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
name|regionNode
operator|.
name|setProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|regionNode
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|addChildProcedure
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
name|setNextState
argument_list|(
name|ReopenTableRegionsState
operator|.
name|REOPEN_TABLE_REGIONS_CONFIRM_REOPENED
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|REOPEN_TABLE_REGIONS_CONFIRM_REOPENED
case|:
name|regions
operator|=
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|::
name|checkReopened
argument_list|)
operator|.
name|filter
argument_list|(
name|l
lambda|->
name|l
operator|!=
literal|null
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
if|if
condition|(
name|regions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
if|if
condition|(
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|l
lambda|->
name|l
operator|.
name|getSeqNum
argument_list|()
operator|>=
literal|0
argument_list|)
condition|)
block|{
name|setNextState
argument_list|(
name|ReopenTableRegionsState
operator|.
name|REOPEN_TABLE_REGIONS_REOPEN_REGIONS
argument_list|)
block|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"There are still {} region(s) which need to be reopened for table {} are in "
operator|+
literal|"OPENING state, try again later"
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// All the regions need to reopen are in OPENING state which means we can not schedule any
comment|// MRPs. Then sleep for one second, and yield the procedure to let other procedures run
comment|// first and hope next time we can get some regions in other state to make progress.
comment|// TODO: add a delay for ProcedureYieldException so that we do not need to sleep here which
comment|// blocks a procedure worker.
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ProcedureYieldException
argument_list|()
throw|;
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
end_class

begin_function
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|ReopenTableRegionsState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|ReopenTableRegionsState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ReopenTableRegionsState
operator|.
name|forNumber
argument_list|(
name|stateId
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|int
name|getStateId
parameter_list|(
name|ReopenTableRegionsState
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
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|ReopenTableRegionsState
name|getInitialState
parameter_list|()
block|{
return|return
name|ReopenTableRegionsState
operator|.
name|REOPEN_TABLE_REGIONS_GET_REGIONS
return|;
block|}
end_function

begin_function
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
name|ReopenTableRegionsStateData
operator|.
name|Builder
name|builder
init|=
name|ReopenTableRegionsStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toRegionLocation
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|builder
operator|::
name|addRegion
argument_list|)
expr_stmt|;
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
end_function

begin_function
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
name|ReopenTableRegionsStateData
name|data
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|ReopenTableRegionsStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|tableName
operator|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|data
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|regions
operator|=
name|data
operator|.
name|getRegionList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toRegionLocation
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
block|}
end_function

unit|}
end_unit

