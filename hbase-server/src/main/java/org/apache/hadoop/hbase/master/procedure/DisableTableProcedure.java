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
name|TableNotEnabledException
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
name|constraint
operator|.
name|ConstraintException
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
name|TableStateManager
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
name|DisableTableState
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DisableTableProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|DisableTableState
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
name|DisableTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|boolean
name|skipTableStateCheck
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
init|=
literal|null
decl_stmt|;
specifier|public
name|DisableTableProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableName the table to operate on    * @param skipTableStateCheck whether to check table state    */
specifier|public
name|DisableTableProcedure
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
name|boolean
name|skipTableStateCheck
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|skipTableStateCheck
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableName the table to operate on    * @param skipTableStateCheck whether to check table state    */
specifier|public
name|DisableTableProcedure
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
name|boolean
name|skipTableStateCheck
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
name|this
operator|.
name|skipTableStateCheck
operator|=
name|skipTableStateCheck
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
name|DisableTableState
name|state
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
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
name|DISABLE_TABLE_PREPARE
case|:
if|if
condition|(
name|prepareDisable
argument_list|(
name|env
argument_list|)
condition|)
block|{
name|setNextState
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_PRE_OPERATION
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|isFailed
argument_list|()
operator|:
literal|"disable should have an exception here"
assert|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
break|break;
case|case
name|DISABLE_TABLE_PRE_OPERATION
case|:
name|preDisable
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_SET_DISABLING_TABLE_STATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DISABLE_TABLE_SET_DISABLING_TABLE_STATE
case|:
name|setTableStateToDisabling
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_MARK_REGIONS_OFFLINE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DISABLE_TABLE_MARK_REGIONS_OFFLINE
case|:
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createUnassignProcedures
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_SET_DISABLED_TABLE_STATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|DISABLE_TABLE_SET_DISABLED_TABLE_STATE
case|:
name|setTableStateToDisabled
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DisableTableState
operator|.
name|DISABLE_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|DISABLE_TABLE_POST_OPERATION
case|:
name|postDisable
argument_list|(
name|env
argument_list|,
name|state
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
literal|"Unhandled state="
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
literal|"master-disable-table"
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
literal|"Retriable error trying to disable table="
operator|+
name|tableName
operator|+
literal|" (in state="
operator|+
name|state
operator|+
literal|")"
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
name|DisableTableState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to rollback, prepare-disable is just table-state checks.
comment|// We can fail if the table does not exist or is not disabled.
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|DISABLE_TABLE_PRE_OPERATION
case|:
return|return;
case|case
name|DISABLE_TABLE_PREPARE
case|:
name|releaseSyncLatch
argument_list|()
expr_stmt|;
return|return;
default|default:
break|break;
block|}
comment|// The delete doesn't have a rollback. The execution will succeed, at some point.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unhandled state="
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
name|DisableTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|DISABLE_TABLE_PREPARE
case|:
case|case
name|DISABLE_TABLE_PRE_OPERATION
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
name|DisableTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|DisableTableState
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
name|DisableTableState
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
name|DisableTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|DisableTableState
operator|.
name|DISABLE_TABLE_PREPARE
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
name|DisableTableStateData
operator|.
name|Builder
name|disableTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|DisableTableStateData
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
operator|.
name|setSkipTableStateCheck
argument_list|(
name|skipTableStateCheck
argument_list|)
decl_stmt|;
name|disableTableMsg
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
name|DisableTableStateData
name|disableTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|DisableTableStateData
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
name|disableTableMsg
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
name|disableTableMsg
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|skipTableStateCheck
operator|=
name|disableTableMsg
operator|.
name|getSkipTableStateCheck
argument_list|()
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
name|DISABLE
return|;
block|}
comment|/**    * Action before any real action of disabling table. Set the exception in the procedure instead    * of throwing it.  This approach is to deal with backward compatible with 1.0.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|boolean
name|prepareDisable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|canTableBeDisabled
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
name|setFailure
argument_list|(
literal|"master-disable-table"
argument_list|,
operator|new
name|ConstraintException
argument_list|(
literal|"Cannot disable catalog table"
argument_list|)
argument_list|)
expr_stmt|;
name|canTableBeDisabled
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
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
literal|"master-disable-table"
argument_list|,
operator|new
name|TableNotFoundException
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|canTableBeDisabled
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|skipTableStateCheck
condition|)
block|{
comment|// There could be multiple client requests trying to disable or enable
comment|// the table at the same time. Ensure only the first request is honored
comment|// After that, no other requests can be accepted until the table reaches
comment|// DISABLED or ENABLED.
comment|//
comment|// Note: in 1.0 release, we called TableStateManager.setTableStateIfInStates() to set
comment|// the state to DISABLING from ENABLED. The implementation was done before table lock
comment|// was implemented. With table lock, there is no need to set the state here (it will
comment|// set the state later on). A quick state check should be enough for us to move forward.
name|TableStateManager
name|tsm
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
decl_stmt|;
name|TableState
operator|.
name|State
name|state
init|=
name|tsm
operator|.
name|getTableState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|state
operator|.
name|equals
argument_list|(
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" isn't enabled;is "
operator|+
name|state
operator|.
name|name
argument_list|()
operator|+
literal|"; skipping disable"
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-disable-table"
argument_list|,
operator|new
name|TableNotEnabledException
argument_list|(
name|tableName
operator|+
literal|" state is "
operator|+
name|state
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|canTableBeDisabled
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// We are done the check. Future actions in this procedure could be done asynchronously.
name|releaseSyncLatch
argument_list|()
expr_stmt|;
return|return
name|canTableBeDisabled
return|;
block|}
comment|/**    * Action before disabling table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|protected
name|void
name|preDisable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|DisableTableState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|runCoprocessorAction
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
comment|/**    * Mark table state to Disabling    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|setTableStateToDisabling
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
comment|// Set table disabling flag up in zk.
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
name|DISABLING
argument_list|)
expr_stmt|;
block|}
comment|/**    * Mark table state to Disabled    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|setTableStateToDisabled
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
comment|// Flip the table to disabled
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
name|DISABLED
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Disabled table, "
operator|+
name|tableName
operator|+
literal|", is completed."
argument_list|)
expr_stmt|;
block|}
comment|/**    * Action after disabling table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|protected
name|void
name|postDisable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|DisableTableState
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|runCoprocessorAction
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
comment|/**    * The procedure could be restarted from a different machine. If the variable is null, we need to    * retrieve it.    * @return traceEnabled    */
specifier|private
name|Boolean
name|isTraceEnabled
parameter_list|()
block|{
if|if
condition|(
name|traceEnabled
operator|==
literal|null
condition|)
block|{
name|traceEnabled
operator|=
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
expr_stmt|;
block|}
return|return
name|traceEnabled
return|;
block|}
comment|/**    * Coprocessor Action.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|runCoprocessorAction
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|DisableTableState
name|state
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
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|DISABLE_TABLE_PRE_OPERATION
case|:
name|cpHost
operator|.
name|preDisableTableAction
argument_list|(
name|tableName
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DISABLE_TABLE_POST_OPERATION
case|:
name|cpHost
operator|.
name|postCompletedDisableTableAction
argument_list|(
name|tableName
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
break|break;
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
block|}
block|}
end_class

end_unit

