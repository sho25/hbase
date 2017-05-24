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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|fs
operator|.
name|FileStatus
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
name|TableNotDisabledException
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
name|TableNotFoundException
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
name|classification
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
name|client
operator|.
name|Connection
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
name|Delete
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|HBaseException
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
name|mob
operator|.
name|MobConstants
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
name|mob
operator|.
name|MobUtils
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
name|DeleteTableState
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
name|HRegion
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DeleteTableProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|DeleteTableState
argument_list|>
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
name|DeleteTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|DeleteTableProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DeleteTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DeleteTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
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
name|tableName
operator|=
name|tableName
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
name|DeleteTableState
name|state
parameter_list|)
throws|throws
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
name|DELETE_TABLE_PRE_OPERATION
case|:
comment|// Verify if we can delete the table
name|boolean
name|deletable
init|=
name|prepareDelete
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|releaseSyncLatch
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|deletable
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
comment|// TODO: Move out... in the acquireLock()
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting for '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' regions in transition"
argument_list|)
expr_stmt|;
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
name|getRegionsOfTable
argument_list|(
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
assert|assert
name|regions
operator|!=
literal|null
operator|&&
operator|!
name|regions
operator|.
name|isEmpty
argument_list|()
operator|:
literal|"unexpected 0 regions"
assert|;
name|ProcedureSyncWait
operator|.
name|waitRegionInTransition
argument_list|(
name|env
argument_list|,
name|regions
argument_list|)
expr_stmt|;
comment|// Call coprocessors
name|preDelete
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_REMOVE_FROM_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_TABLE_REMOVE_FROM_META
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"delete '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' regions from META"
argument_list|)
expr_stmt|;
name|DeleteTableProcedure
operator|.
name|deleteFromMeta
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_CLEAR_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_TABLE_CLEAR_FS_LAYOUT
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"delete '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' from filesystem"
argument_list|)
expr_stmt|;
name|DeleteTableProcedure
operator|.
name|deleteFromFs
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|regions
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_UPDATE_DESC_CACHE
argument_list|)
expr_stmt|;
name|regions
operator|=
literal|null
expr_stmt|;
break|break;
case|case
name|DELETE_TABLE_UPDATE_DESC_CACHE
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"delete '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' descriptor"
argument_list|)
expr_stmt|;
name|DeleteTableProcedure
operator|.
name|deleteTableDescriptorCache
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_UNASSIGN_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_TABLE_UNASSIGN_REGIONS
case|:
name|LOG
operator|.
name|debug
argument_list|(
literal|"delete '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' assignment state"
argument_list|)
expr_stmt|;
name|DeleteTableProcedure
operator|.
name|deleteAssignmentState
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteTableState
operator|.
name|DELETE_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_TABLE_POST_OPERATION
case|:
name|postDelete
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"delete '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' completed"
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
name|HBaseException
decl||
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
literal|"master-delete-table"
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
literal|"Retriable error trying to delete table="
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
name|boolean
name|abort
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// TODO: Current behavior is: with no rollback and no abort support, procedure may stuck
comment|// looping in retrying failing step forever. Default behavior of abort is changed to support
comment|// aborting all procedures. Override the default wisely. Following code retains the current
comment|// behavior. Revisit it later.
return|return
name|isRollbackSupported
argument_list|(
name|getCurrentState
argument_list|()
argument_list|)
condition|?
name|super
operator|.
name|abort
argument_list|(
name|env
argument_list|)
else|:
literal|false
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
name|DeleteTableState
name|state
parameter_list|)
block|{
if|if
condition|(
name|state
operator|==
name|DeleteTableState
operator|.
name|DELETE_TABLE_PRE_OPERATION
condition|)
block|{
comment|// nothing to rollback, pre-delete is just table-state checks.
comment|// We can fail if the table does not exist or is not disabled.
comment|// TODO: coprocessor rollback semantic is still undefined.
name|releaseSyncLatch
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// The delete doesn't have a rollback. The execution will succeed, at some point.
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
name|DeleteTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|DELETE_TABLE_PRE_OPERATION
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
name|DeleteTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|DeleteTableState
operator|.
name|valueOf
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
name|DeleteTableState
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
name|DeleteTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|DeleteTableState
operator|.
name|DELETE_TABLE_PRE_OPERATION
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
name|DELETE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serializeStateData
parameter_list|(
specifier|final
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|serializeStateData
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|DeleteTableStateData
operator|.
name|Builder
name|state
init|=
name|MasterProcedureProtos
operator|.
name|DeleteTableStateData
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
if|if
condition|(
name|regions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|state
operator|.
name|addRegionInfo
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
name|state
operator|.
name|build
argument_list|()
operator|.
name|writeDelimitedTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deserializeStateData
parameter_list|(
specifier|final
name|InputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|deserializeStateData
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|MasterProcedureProtos
operator|.
name|DeleteTableStateData
name|state
init|=
name|MasterProcedureProtos
operator|.
name|DeleteTableStateData
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
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
name|tableName
operator|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|state
operator|.
name|getTableName
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
name|regions
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|regions
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
name|regions
operator|.
name|add
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
block|}
specifier|private
name|boolean
name|prepareDelete
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|checkTableModifiable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableNotFoundException
decl||
name|TableNotDisabledException
name|e
parameter_list|)
block|{
name|setFailure
argument_list|(
literal|"master-delete-table"
argument_list|,
name|e
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
name|boolean
name|preDelete
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
name|TableName
name|tableName
init|=
name|this
operator|.
name|tableName
decl_stmt|;
name|cpHost
operator|.
name|preDeleteTableAction
argument_list|(
name|tableName
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|postDelete
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
name|deleteTableStates
argument_list|(
name|env
argument_list|,
name|tableName
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
specifier|final
name|TableName
name|tableName
init|=
name|this
operator|.
name|tableName
decl_stmt|;
name|cpHost
operator|.
name|postCompletedDeleteTableAction
argument_list|(
name|tableName
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|deleteFromFs
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|,
specifier|final
name|boolean
name|archive
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
name|FileSystem
name|fs
init|=
name|mfs
operator|.
name|getFileSystem
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
name|tableName
argument_list|)
decl_stmt|;
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
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tableDir
argument_list|)
condition|)
block|{
comment|// Ensure temp exists
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tempdir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tempdir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase temp directory '"
operator|+
name|tempdir
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
comment|// Ensure parent exists
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tempTableDir
operator|.
name|getParent
argument_list|()
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|tempTableDir
operator|.
name|getParent
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HBase temp directory '"
operator|+
name|tempdir
operator|+
literal|"' creation failure."
argument_list|)
throw|;
block|}
comment|// Move the table in /hbase/.tmp
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|tableDir
argument_list|,
name|tempTableDir
argument_list|)
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|tempTableDir
argument_list|)
condition|)
block|{
comment|// TODO
comment|// what's in this dir? something old? probably something manual from the user...
comment|// let's get rid of this stuff...
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tempdir
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
operator|&&
name|files
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
operator|!
name|files
index|[
name|i
index|]
operator|.
name|isDir
argument_list|()
condition|)
continue|continue;
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tempTableDir
argument_list|,
name|files
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|fs
operator|.
name|delete
argument_list|(
name|tempdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to move '"
operator|+
name|tableDir
operator|+
literal|"' to temp '"
operator|+
name|tempTableDir
operator|+
literal|"'"
argument_list|)
throw|;
block|}
block|}
comment|// Archive regions from FS (temp directory)
if|if
condition|(
name|archive
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Archiving region "
operator|+
name|hri
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" from FS"
argument_list|)
expr_stmt|;
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|tempTableDir
argument_list|,
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|tempTableDir
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Table '"
operator|+
name|tableName
operator|+
literal|"' archived!"
argument_list|)
expr_stmt|;
block|}
comment|// Archive mob data
name|Path
name|mobTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|MobConstants
operator|.
name|MOB_DIR_NAME
argument_list|)
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|mobTableDir
argument_list|,
name|MobUtils
operator|.
name|getMobRegionInfo
argument_list|(
name|tableName
argument_list|)
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
condition|)
block|{
name|HFileArchiver
operator|.
name|archiveRegion
argument_list|(
name|fs
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|mobTableDir
argument_list|,
name|regionDir
argument_list|)
expr_stmt|;
block|}
comment|// Delete table directory from FS (temp directory)
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|tempTableDir
argument_list|,
literal|true
argument_list|)
operator|&&
name|fs
operator|.
name|exists
argument_list|(
name|tempTableDir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't delete "
operator|+
name|tempTableDir
argument_list|)
throw|;
block|}
comment|// Delete the table directory where the mob files are saved
if|if
condition|(
name|mobTableDir
operator|!=
literal|null
operator|&&
name|fs
operator|.
name|exists
argument_list|(
name|mobTableDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|mobTableDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't delete mob dir "
operator|+
name|mobTableDir
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * There may be items for this table still up in hbase:meta in the case where the    * info:regioninfo column was empty because of some write error. Remove ALL rows from hbase:meta    * that have to do with this table. See HBASE-12980.    * @throws IOException    */
specifier|private
specifier|static
name|void
name|cleanAnyRemainingRows
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
name|Connection
name|connection
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|Scan
name|tableScan
init|=
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
try|try
init|(
name|Table
name|metaTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
init|)
block|{
name|List
argument_list|<
name|Delete
argument_list|>
name|deletes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|ResultScanner
name|resScanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|tableScan
argument_list|)
init|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|resScanner
control|)
block|{
name|deletes
operator|.
name|add
argument_list|(
operator|new
name|Delete
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|deletes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Deleting some vestigial "
operator|+
name|deletes
operator|.
name|size
argument_list|()
operator|+
literal|" rows of "
operator|+
name|tableName
operator|+
literal|" from "
operator|+
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|metaTable
operator|.
name|delete
argument_list|(
name|deletes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|static
name|void
name|deleteFromMeta
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
throws|throws
name|IOException
block|{
name|MetaTableAccessor
operator|.
name|deleteRegions
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|regions
argument_list|)
expr_stmt|;
comment|// Clean any remaining rows for this table.
name|cleanAnyRemainingRows
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// clean region references from the server manager
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerManager
argument_list|()
operator|.
name|removeRegions
argument_list|(
name|regions
argument_list|)
expr_stmt|;
comment|// Clear Favored Nodes for this table
name|FavoredNodesManager
name|fnm
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
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
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|void
name|deleteAssignmentState
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
comment|// Clean up regions of the table in RegionStates.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing '"
operator|+
name|tableName
operator|+
literal|"' from region states."
argument_list|)
expr_stmt|;
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// If entry for this table states, remove it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking '"
operator|+
name|tableName
operator|+
literal|"' as deleted."
argument_list|)
expr_stmt|;
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|setDeletedTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|deleteTableDescriptorCache
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing '"
operator|+
name|tableName
operator|+
literal|"' descriptor."
argument_list|)
expr_stmt|;
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|remove
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|deleteTableStates
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
if|if
condition|(
operator|!
name|tableName
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
name|removeTableFromNamespaceQuota
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

