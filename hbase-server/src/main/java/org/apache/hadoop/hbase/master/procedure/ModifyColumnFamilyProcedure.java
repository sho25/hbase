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
name|InvalidFamilyOperationException
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|TableDescriptorBuilder
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
name|ModifyColumnFamilyState
import|;
end_import

begin_comment
comment|/**  * The procedure to modify a column family from an existing table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModifyColumnFamilyProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|ModifyColumnFamilyState
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
name|ModifyColumnFamilyProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|TableDescriptor
name|unmodifiedtableDescriptor
decl_stmt|;
specifier|private
name|ColumnFamilyDescriptor
name|cfDescriptor
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
decl_stmt|;
specifier|public
name|ModifyColumnFamilyProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|unmodifiedtableDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|ModifyColumnFamilyProcedure
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
name|ColumnFamilyDescriptor
name|cfDescriptor
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|cfDescriptor
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ModifyColumnFamilyProcedure
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
name|ColumnFamilyDescriptor
name|cfDescriptor
parameter_list|,
specifier|final
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
name|cfDescriptor
operator|=
name|cfDescriptor
expr_stmt|;
name|this
operator|.
name|unmodifiedtableDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
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
name|ModifyColumnFamilyState
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
name|MODIFY_COLUMN_FAMILY_PREPARE
case|:
name|prepareModify
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
case|:
name|preModify
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
case|:
name|updateTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_POST_OPERATION
case|:
name|postModify
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_REOPEN_ALL_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_REOPEN_ALL_REGIONS
case|:
if|if
condition|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createReopenProcedures
argument_list|(
name|getTableName
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
literal|"master-modify-columnfamily"
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
name|ModifyColumnFamilyState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_PREPARE
operator|||
name|state
operator|==
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
condition|)
block|{
comment|// nothing to rollback, pre-modify is just checks.
comment|// TODO: coprocessor rollback semantic is still undefined.
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
name|ModifyColumnFamilyState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
case|:
case|case
name|MODIFY_COLUMN_FAMILY_PREPARE
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
name|ModifyColumnFamilyState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ModifyColumnFamilyState
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
name|ModifyColumnFamilyState
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
name|ModifyColumnFamilyState
name|getInitialState
parameter_list|()
block|{
return|return
name|ModifyColumnFamilyState
operator|.
name|MODIFY_COLUMN_FAMILY_PREPARE
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
name|ModifyColumnFamilyStateData
operator|.
name|Builder
name|modifyCFMsg
init|=
name|MasterProcedureProtos
operator|.
name|ModifyColumnFamilyStateData
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
name|setColumnfamilySchema
argument_list|(
name|ProtobufUtil
operator|.
name|toColumnFamilySchema
argument_list|(
name|cfDescriptor
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|unmodifiedtableDescriptor
operator|!=
literal|null
condition|)
block|{
name|modifyCFMsg
operator|.
name|setUnmodifiedTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|toTableSchema
argument_list|(
name|unmodifiedtableDescriptor
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|modifyCFMsg
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
name|ModifyColumnFamilyStateData
name|modifyCFMsg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|ModifyColumnFamilyStateData
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
name|modifyCFMsg
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
name|modifyCFMsg
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|cfDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toColumnFamilyDescriptor
argument_list|(
name|modifyCFMsg
operator|.
name|getColumnfamilySchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|modifyCFMsg
operator|.
name|hasUnmodifiedTableSchema
argument_list|()
condition|)
block|{
name|unmodifiedtableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toTableDescriptor
argument_list|(
name|modifyCFMsg
operator|.
name|getUnmodifiedTableSchema
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|tableName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", columnfamily="
argument_list|)
expr_stmt|;
if|if
condition|(
name|cfDescriptor
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Unknown"
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * Action before any real action of modifying column family.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareModify
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Checks whether the table is allowed to be modified.
name|checkTableModifiable
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|unmodifiedtableDescriptor
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
if|if
condition|(
name|unmodifiedtableDescriptor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"TableDescriptor missing for "
operator|+
name|tableName
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|unmodifiedtableDescriptor
operator|.
name|hasColumnFamily
argument_list|(
name|cfDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidFamilyOperationException
argument_list|(
literal|"Family '"
operator|+
name|getColumnFamilyName
argument_list|()
operator|+
literal|"' does not exist, so it cannot be modified"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Action before modifying column family.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|preModify
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ModifyColumnFamilyState
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
comment|/**    * Modify the column family from the file system    */
specifier|private
name|void
name|updateTableDescriptor
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Update table descriptor
name|LOG
operator|.
name|info
argument_list|(
literal|"ModifyColumnFamily. Table = "
operator|+
name|tableName
operator|+
literal|" HCD = "
operator|+
name|cfDescriptor
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
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
argument_list|)
decl_stmt|;
name|builder
operator|.
name|modifyColumnFamily
argument_list|(
name|cfDescriptor
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
name|add
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Restore back to the old descriptor    * @param env MasterProcedureEnv    * @throws IOException    **/
specifier|private
name|void
name|restoreTableDescriptor
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
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
name|add
argument_list|(
name|unmodifiedtableDescriptor
argument_list|)
expr_stmt|;
comment|// Make sure regions are opened after table descriptor is updated.
comment|//reOpenAllRegionsIfTableIsOnline(env);
comment|// TODO: NUKE ROLLBACK!!!!
block|}
comment|/**    * Action after modifying column family.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|postModify
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ModifyColumnFamilyState
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
specifier|private
name|String
name|getColumnFamilyName
parameter_list|()
block|{
return|return
name|cfDescriptor
operator|.
name|getNameAsString
argument_list|()
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
name|ModifyColumnFamilyState
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
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
case|:
name|cpHost
operator|.
name|preModifyColumnFamilyAction
argument_list|(
name|tableName
argument_list|,
name|cfDescriptor
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_POST_OPERATION
case|:
name|cpHost
operator|.
name|postCompletedModifyColumnFamilyAction
argument_list|(
name|tableName
argument_list|,
name|cfDescriptor
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

