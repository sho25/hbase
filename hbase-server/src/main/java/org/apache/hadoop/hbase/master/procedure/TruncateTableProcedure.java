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
name|InputStream
import|;
end_import

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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Arrays
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
name|HTableDescriptor
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
name|protobuf
operator|.
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|TruncateTableState
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
name|util
operator|.
name|ModifyRegionUtils
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TruncateTableProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|TruncateTableState
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
name|TruncateTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|preserveSplits
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
decl_stmt|;
specifier|private
name|HTableDescriptor
name|hTableDescriptor
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|TruncateTableProcedure
parameter_list|()
block|{
comment|// Required by the Procedure framework to create the procedure on replay
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TruncateTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|boolean
name|preserveSplits
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|preserveSplits
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TruncateTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
name|boolean
name|preserveSplits
parameter_list|,
name|ProcedurePrepareLatch
name|latch
parameter_list|)
block|{
name|super
argument_list|(
name|env
argument_list|,
name|latch
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|preserveSplits
operator|=
name|preserveSplits
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
name|TruncateTableState
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
name|TRUNCATE_TABLE_PRE_OPERATION
case|:
comment|// Verify if we can truncate the table
if|if
condition|(
operator|!
name|prepareTruncate
argument_list|(
name|env
argument_list|)
condition|)
block|{
assert|assert
name|isFailed
argument_list|()
operator|:
literal|"the truncate should have an exception here"
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
literal|"waiting for '"
operator|+
name|getTableName
argument_list|()
operator|+
literal|"' regions in transition"
argument_list|)
expr_stmt|;
name|regions
operator|=
name|ProcedureSyncWait
operator|.
name|getRegionsFromMeta
argument_list|(
name|env
argument_list|,
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
name|preTruncate
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_REMOVE_FROM_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_REMOVE_FROM_META
case|:
name|hTableDescriptor
operator|=
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
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_CLEAR_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_CLEAR_FS_LAYOUT
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
name|regions
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|preserveSplits
condition|)
block|{
comment|// if we are not preserving splits, generate a new single region
name|regions
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|ModifyRegionUtils
operator|.
name|createHRegionInfos
argument_list|(
name|hTableDescriptor
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regions
operator|=
name|recreateRegionInfo
argument_list|(
name|regions
argument_list|)
expr_stmt|;
block|}
name|setNextState
argument_list|(
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_CREATE_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_CREATE_FS_LAYOUT
case|:
name|regions
operator|=
name|CreateTableProcedure
operator|.
name|createFsLayout
argument_list|(
name|env
argument_list|,
name|hTableDescriptor
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|CreateTableProcedure
operator|.
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
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_ADD_TO_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_ADD_TO_META
case|:
name|regions
operator|=
name|CreateTableProcedure
operator|.
name|addTableToMeta
argument_list|(
name|env
argument_list|,
name|hTableDescriptor
argument_list|,
name|regions
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_ASSIGN_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_ASSIGN_REGIONS
case|:
name|CreateTableProcedure
operator|.
name|assignRegions
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
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
name|hTableDescriptor
operator|=
literal|null
expr_stmt|;
name|regions
operator|=
literal|null
expr_stmt|;
break|break;
case|case
name|TRUNCATE_TABLE_POST_OPERATION
case|:
name|postTruncate
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"truncate '"
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
literal|"master-truncate-table"
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
literal|"Retriable error trying to truncate table="
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
name|TruncateTableState
name|state
parameter_list|)
block|{
if|if
condition|(
name|state
operator|==
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_PRE_OPERATION
condition|)
block|{
comment|// nothing to rollback, pre-truncate is just table-state checks.
comment|// We can fail if the table does not exist or is not disabled.
comment|// TODO: coprocessor rollback semantic is still undefined.
return|return;
block|}
comment|// The truncate doesn't have a rollback. The execution will succeed, at some point.
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
name|void
name|completionCleanup
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|releaseSyncLatch
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|TruncateTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|TRUNCATE_TABLE_PRE_OPERATION
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
name|TruncateTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|TruncateTableState
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
name|TruncateTableState
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
name|TruncateTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|TruncateTableState
operator|.
name|TRUNCATE_TABLE_PRE_OPERATION
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
name|EDIT
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|abort
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
comment|// TODO: We may be able to abort if the procedure is not started yet.
return|return
literal|false
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
literal|" (table="
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
literal|" preserveSplits="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|preserveSplits
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
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
name|TruncateTableStateData
operator|.
name|Builder
name|state
init|=
name|MasterProcedureProtos
operator|.
name|TruncateTableStateData
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
name|setPreserveSplits
argument_list|(
name|preserveSplits
argument_list|)
decl_stmt|;
if|if
condition|(
name|hTableDescriptor
operator|!=
literal|null
condition|)
block|{
name|state
operator|.
name|setTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|convertToTableSchema
argument_list|(
name|hTableDescriptor
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|state
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
expr_stmt|;
block|}
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
name|TruncateTableStateData
name|state
init|=
name|MasterProcedureProtos
operator|.
name|TruncateTableStateData
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
if|if
condition|(
name|state
operator|.
name|hasTableSchema
argument_list|()
condition|)
block|{
name|hTableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|convertToHTableDesc
argument_list|(
name|state
operator|.
name|getTableSchema
argument_list|()
argument_list|)
expr_stmt|;
name|tableName
operator|=
name|hTableDescriptor
operator|.
name|getTableName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
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
block|}
name|preserveSplits
operator|=
name|state
operator|.
name|getPreserveSplits
argument_list|()
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
argument_list|<
name|HRegionInfo
argument_list|>
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
specifier|static
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|recreateRegionInfo
parameter_list|(
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
name|newRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regions
control|)
block|{
name|newRegions
operator|.
name|add
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newRegions
return|;
block|}
specifier|private
name|boolean
name|prepareTruncate
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
name|getTableName
argument_list|()
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
literal|"master-truncate-table"
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
name|preTruncate
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
name|getTableName
argument_list|()
decl_stmt|;
name|cpHost
operator|.
name|preTruncateTableAction
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
name|postTruncate
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
name|getTableName
argument_list|()
decl_stmt|;
name|cpHost
operator|.
name|postCompletedTruncateTableAction
argument_list|(
name|tableName
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

