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
name|ArrayList
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
name|fs
operator|.
name|Path
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
name|DoNotRetryIOException
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
name|TableExistsException
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
name|client
operator|.
name|TableDescriptor
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
name|master
operator|.
name|MasterCoprocessorHost
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
name|MasterFileSystem
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
name|util
operator|.
name|FSTableDescriptors
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
name|util
operator|.
name|FSUtils
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
name|util
operator|.
name|ModifyRegionUtils
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
name|util
operator|.
name|ServerRegionReplicaUtil
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
name|HBaseProtos
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
name|CreateTableState
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CreateTableProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|CreateTableState
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
name|CreateTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableDescriptor
name|tableDescriptor
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
decl_stmt|;
specifier|public
name|CreateTableProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CreateTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|newRegions
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CreateTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|RegionInfo
index|[]
name|newRegions
parameter_list|,
specifier|final
name|ProcedurePrepareLatch
name|syncLatch
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|,
name|syncLatch
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableDescriptor
operator|=
name|tableDescriptor
expr_stmt|;
name|this
operator|.
name|newRegions
operator|=
name|newRegions
operator|!=
literal|null
condition|?
name|Lists
operator|.
name|newArrayList
argument_list|(
name|newRegions
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Flow
name|executeFromState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|CreateTableState
name|state
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"{} execute state={}"
argument_list|,
name|this
argument_list|,
name|state
argument_list|)
expr_stmt|;
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|CREATE_TABLE_PRE_OPERATION
case|:
comment|// Verify if we can create the table
name|boolean
name|exists
init|=
operator|!
name|prepareCreate
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|releaseSyncLatch
argument_list|()
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
assert|assert
name|isFailed
argument_list|()
operator|:
literal|"the delete should have an exception here"
assert|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
name|preCreate
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_WRITE_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_TABLE_WRITE_FS_LAYOUT
case|:
name|DeleteTableProcedure
operator|.
name|deleteFromFs
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|newRegions
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|newRegions
operator|=
name|createFsLayout
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_ADD_TO_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_TABLE_ADD_TO_META
case|:
name|newRegions
operator|=
name|addTableToMeta
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_ASSIGN_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_TABLE_ASSIGN_REGIONS
case|:
name|setEnablingState
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createRoundRobinAssignProcedures
argument_list|(
name|newRegions
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_UPDATE_DESC_CACHE
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_TABLE_UPDATE_DESC_CACHE
case|:
name|setEnabledState
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|updateTableDescCache
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CreateTableState
operator|.
name|CREATE_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_TABLE_POST_OPERATION
case|:
name|postCreate
argument_list|(
name|env
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
if|if
condition|(
name|isRollbackSupported
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|setFailure
argument_list|(
literal|"master-create-table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Retriable error trying to create table="
operator|+
name|getTableName
argument_list|()
operator|+
literal|" state="
operator|+
name|state
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|CreateTableState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|CreateTableState
operator|.
name|CREATE_TABLE_PRE_OPERATION
condition|)
block|{
comment|// nothing to rollback, pre-create is just table-state checks.
comment|// We can fail if the table does exist or the descriptor is malformed.
comment|// TODO: coprocessor rollback semantic is still undefined.
if|if
condition|(
name|hasException
argument_list|()
comment|/* avoid NPE */
operator|&&
name|getException
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
operator|!=
name|TableExistsException
operator|.
name|class
condition|)
block|{
name|DeleteTableProcedure
operator|.
name|deleteTableStates
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|MasterCoprocessorHost
name|cpHost
init|=
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postDeleteTable
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|releaseSyncLatch
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// The procedure doesn't have a rollback. The execution will succeed, at some point.
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
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|CreateTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|CREATE_TABLE_PRE_OPERATION
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
name|CreateTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|CreateTableState
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
specifier|final
name|CreateTableState
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
name|CreateTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|CreateTableState
operator|.
name|CREATE_TABLE_PRE_OPERATION
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
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
name|CREATE
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
name|MasterProcedureProtos
operator|.
name|CreateTableStateData
operator|.
name|Builder
name|state
init|=
name|MasterProcedureProtos
operator|.
name|CreateTableStateData
operator|.
name|newBuilder
argument_list|()
operator|.
name|setUserInfo
argument_list|(
name|MasterProcedureUtil
operator|.
name|toProtoUserInfo
argument_list|(
name|getUser
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|toTableSchema
argument_list|(
name|tableDescriptor
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|newRegions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|RegionInfo
name|hri
range|:
name|newRegions
control|)
block|{
name|state
operator|.
name|addRegionInfo
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
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
name|CreateTableStateData
name|state
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|CreateTableStateData
operator|.
name|class
argument_list|)
decl_stmt|;
name|setUser
argument_list|(
name|MasterProcedureUtil
operator|.
name|toUserInfo
argument_list|(
name|state
operator|.
name|getUserInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toTableDescriptor
argument_list|(
name|state
operator|.
name|getTableSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|.
name|getRegionInfoCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|newRegions
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|newRegions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|state
operator|.
name|getRegionInfoCount
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|RegionInfo
name|hri
range|:
name|state
operator|.
name|getRegionInfoList
argument_list|()
control|)
block|{
name|newRegions
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|waitInitialized
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
name|getTableName
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
comment|// Creating system table is part of the initialization, so do not wait here.
return|return
literal|false
return|;
block|}
return|return
name|super
operator|.
name|waitInitialized
argument_list|(
name|env
argument_list|)
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
name|getTableName
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
specifier|private
name|boolean
name|prepareCreate
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|TableName
name|tableName
init|=
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
name|MetaTableAccessor
operator|.
name|tableExists
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
condition|)
block|{
name|setFailure
argument_list|(
literal|"master-create-table"
argument_list|,
operator|new
name|TableExistsException
argument_list|(
name|getTableName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// check that we have at least 1 CF
if|if
condition|(
name|tableDescriptor
operator|.
name|getColumnFamilyCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|setFailure
argument_list|(
literal|"master-create-table"
argument_list|,
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Table "
operator|+
name|getTableName
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" should have at least one column family."
argument_list|)
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
specifier|private
name|void
name|preCreate
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
operator|!
name|getTableName
argument_list|()
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|ProcedureSyncWait
operator|.
name|getMasterQuotaManager
argument_list|(
name|env
argument_list|)
operator|.
name|checkNamespaceTableAndRegionQuota
argument_list|(
name|getTableName
argument_list|()
argument_list|,
operator|(
name|newRegions
operator|!=
literal|null
condition|?
name|newRegions
operator|.
name|size
argument_list|()
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|MasterCoprocessorHost
name|cpHost
init|=
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
specifier|final
name|RegionInfo
index|[]
name|regions
init|=
name|newRegions
operator|==
literal|null
condition|?
literal|null
else|:
name|newRegions
operator|.
name|toArray
argument_list|(
operator|new
name|RegionInfo
index|[
name|newRegions
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|cpHost
operator|.
name|preCreateTableAction
argument_list|(
name|tableDescriptor
argument_list|,
name|regions
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|postCreate
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
specifier|final
name|MasterCoprocessorHost
name|cpHost
init|=
name|env
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
specifier|final
name|RegionInfo
index|[]
name|regions
init|=
operator|(
name|newRegions
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|newRegions
operator|.
name|toArray
argument_list|(
operator|new
name|RegionInfo
index|[
name|newRegions
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|cpHost
operator|.
name|postCompletedCreateTableAction
argument_list|(
name|tableDescriptor
argument_list|,
name|regions
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
interface|interface
name|CreateHdfsRegions
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createHdfsRegions
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|Path
name|tableRootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|protected
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createFsLayout
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createFsLayout
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|,
operator|new
name|CreateHdfsRegions
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createHdfsRegions
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|Path
name|tableRootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
parameter_list|)
throws|throws
name|IOException
block|{
name|RegionInfo
index|[]
name|regions
init|=
name|newRegions
operator|!=
literal|null
condition|?
name|newRegions
operator|.
name|toArray
argument_list|(
operator|new
name|RegionInfo
index|[
name|newRegions
operator|.
name|size
argument_list|()
index|]
argument_list|)
else|:
literal|null
decl_stmt|;
return|return
name|ModifyRegionUtils
operator|.
name|createRegions
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|,
name|tableRootDir
argument_list|,
name|tableDescriptor
argument_list|,
name|regions
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createFsLayout
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
parameter_list|,
specifier|final
name|CreateHdfsRegions
name|hdfsRegionHandler
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tempdir
init|=
name|mfs
operator|.
name|getTempDir
argument_list|()
decl_stmt|;
comment|// 1. Create Table Descriptor
comment|// using a copy of descriptor, table will be created enabling first
specifier|final
name|Path
name|tempTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|tempdir
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
operator|(
call|(
name|FSTableDescriptors
call|)
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableDescriptors
argument_list|()
argument_list|)
operator|)
operator|.
name|createTableDescriptorForTableDirectory
argument_list|(
name|tempTableDir
argument_list|,
name|tableDescriptor
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// 2. Create Regions
name|newRegions
operator|=
name|hdfsRegionHandler
operator|.
name|createHdfsRegions
argument_list|(
name|env
argument_list|,
name|tempdir
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
comment|// 3. Move Table temp directory to the hbase root location
name|moveTempDirectoryToHBaseRoot
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|tempTableDir
argument_list|)
expr_stmt|;
return|return
name|newRegions
return|;
block|}
specifier|protected
specifier|static
name|void
name|moveTempDirectoryToHBaseRoot
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|Path
name|tempTableDir
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|MasterFileSystem
name|mfs
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|mfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|tableDir
argument_list|,
literal|true
argument_list|)
operator|&&
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't delete "
operator|+
name|tableDir
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|tempTableDir
argument_list|,
name|tableDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to move table from temp="
operator|+
name|tempTableDir
operator|+
literal|" to hbase root="
operator|+
name|tableDir
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|static
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|addTableToMeta
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|regions
operator|!=
literal|null
operator|&&
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|:
literal|"expected at least 1 region, got "
operator|+
name|regions
assert|;
name|ProcedureSyncWait
operator|.
name|waitMetaRegions
argument_list|(
name|env
argument_list|)
expr_stmt|;
comment|// Add replicas if needed
comment|// we need to create regions with replicaIds starting from 1
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
init|=
name|RegionReplicaUtil
operator|.
name|addReplicas
argument_list|(
name|tableDescriptor
argument_list|,
name|regions
argument_list|,
literal|1
argument_list|,
name|tableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
decl_stmt|;
comment|// Add regions to META
name|addRegionsToMeta
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
comment|// Setup replication for region replicas if needed
if|if
condition|(
name|tableDescriptor
operator|.
name|getRegionReplication
argument_list|()
operator|>
literal|1
condition|)
block|{
name|ServerRegionReplicaUtil
operator|.
name|setupRegionReplicaReplication
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|newRegions
return|;
block|}
specifier|protected
specifier|static
name|void
name|setEnablingState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Mark the table as Enabling
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLING
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|setEnabledState
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Enable table
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setTableState
argument_list|(
name|tableName
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add the specified set of regions to the hbase:meta table.    */
specifier|private
specifier|static
name|void
name|addRegionsToMeta
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|addRegionsToMeta
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regionInfos
argument_list|,
name|tableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|updateTableDescCache
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
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
comment|// system tables are created on bootstrap internally by the system
comment|// the client does not know about this procedures.
return|return
operator|!
name|getTableName
argument_list|()
operator|.
name|isSystemTable
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|RegionInfo
name|getFirstRegionInfo
parameter_list|()
block|{
if|if
condition|(
name|newRegions
operator|==
literal|null
operator|||
name|newRegions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|newRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

