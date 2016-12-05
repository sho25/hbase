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
name|HashSet
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
name|Set
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
name|HConstants
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
name|ModifyTableState
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModifyTableProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|ModifyTableState
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
name|ModifyTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HTableDescriptor
name|unmodifiedHTableDescriptor
init|=
literal|null
decl_stmt|;
specifier|private
name|HTableDescriptor
name|modifiedHTableDescriptor
decl_stmt|;
specifier|private
name|boolean
name|deleteColumnFamilyInModify
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfoList
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
init|=
literal|null
decl_stmt|;
specifier|public
name|ModifyTableProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|initilize
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ModifyTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ModifyTableProcedure
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
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
name|initilize
argument_list|()
expr_stmt|;
name|this
operator|.
name|modifiedHTableDescriptor
operator|=
name|htd
expr_stmt|;
block|}
specifier|private
name|void
name|initilize
parameter_list|()
block|{
name|this
operator|.
name|unmodifiedHTableDescriptor
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|regionInfoList
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|traceEnabled
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|deleteColumnFamilyInModify
operator|=
literal|false
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
name|ModifyTableState
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
name|MODIFY_TABLE_PREPARE
case|:
name|prepareModify
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyTableState
operator|.
name|MODIFY_TABLE_PRE_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_PRE_OPERATION
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
name|ModifyTableState
operator|.
name|MODIFY_TABLE_UPDATE_TABLE_DESCRIPTOR
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_UPDATE_TABLE_DESCRIPTOR
case|:
name|updateTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyTableState
operator|.
name|MODIFY_TABLE_REMOVE_REPLICA_COLUMN
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_REMOVE_REPLICA_COLUMN
case|:
name|updateReplicaColumnsIfNeeded
argument_list|(
name|env
argument_list|,
name|unmodifiedHTableDescriptor
argument_list|,
name|modifiedHTableDescriptor
argument_list|)
expr_stmt|;
if|if
condition|(
name|deleteColumnFamilyInModify
condition|)
block|{
name|setNextState
argument_list|(
name|ModifyTableState
operator|.
name|MODIFY_TABLE_DELETE_FS_LAYOUT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|setNextState
argument_list|(
name|ModifyTableState
operator|.
name|MODIFY_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|MODIFY_TABLE_DELETE_FS_LAYOUT
case|:
name|deleteFromFs
argument_list|(
name|env
argument_list|,
name|unmodifiedHTableDescriptor
argument_list|,
name|modifiedHTableDescriptor
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|ModifyTableState
operator|.
name|MODIFY_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_POST_OPERATION
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
name|ModifyTableState
operator|.
name|MODIFY_TABLE_REOPEN_ALL_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_REOPEN_ALL_REGIONS
case|:
name|reOpenAllRegionsIfTableIsOnline
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
literal|"master-modify-table"
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
literal|"Retriable error trying to modify table="
operator|+
name|getTableName
argument_list|()
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
name|ModifyTableState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|ModifyTableState
operator|.
name|MODIFY_TABLE_PREPARE
operator|||
name|state
operator|==
name|ModifyTableState
operator|.
name|MODIFY_TABLE_PRE_OPERATION
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
name|ModifyTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|MODIFY_TABLE_PRE_OPERATION
case|:
case|case
name|MODIFY_TABLE_PREPARE
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
name|ModifyTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|ModifyTableState
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
name|ModifyTableState
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
name|ModifyTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|ModifyTableState
operator|.
name|MODIFY_TABLE_PREPARE
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
name|ModifyTableStateData
operator|.
name|Builder
name|modifyTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|ModifyTableStateData
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
name|setModifiedTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|convertToTableSchema
argument_list|(
name|modifiedHTableDescriptor
argument_list|)
argument_list|)
operator|.
name|setDeleteColumnFamilyInModify
argument_list|(
name|deleteColumnFamilyInModify
argument_list|)
decl_stmt|;
if|if
condition|(
name|unmodifiedHTableDescriptor
operator|!=
literal|null
condition|)
block|{
name|modifyTableMsg
operator|.
name|setUnmodifiedTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|convertToTableSchema
argument_list|(
name|unmodifiedHTableDescriptor
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|modifyTableMsg
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
name|ModifyTableStateData
name|modifyTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|ModifyTableStateData
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
name|modifyTableMsg
operator|.
name|getUserInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|modifiedHTableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|convertToHTableDesc
argument_list|(
name|modifyTableMsg
operator|.
name|getModifiedTableSchema
argument_list|()
argument_list|)
expr_stmt|;
name|deleteColumnFamilyInModify
operator|=
name|modifyTableMsg
operator|.
name|getDeleteColumnFamilyInModify
argument_list|()
expr_stmt|;
if|if
condition|(
name|modifyTableMsg
operator|.
name|hasUnmodifiedTableSchema
argument_list|()
condition|)
block|{
name|unmodifiedHTableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|convertToHTableDesc
argument_list|(
name|modifyTableMsg
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
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|modifiedHTableDescriptor
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
name|EDIT
return|;
block|}
comment|/**    * Check conditions before any real action of modifying a table.    * @param env MasterProcedureEnv    * @throws IOException    */
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
comment|// Checks whether the table exists
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
name|getTableName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
comment|// check that we have at least 1 CF
if|if
condition|(
name|modifiedHTableDescriptor
operator|.
name|getColumnFamilyCount
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
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
throw|;
block|}
comment|// In order to update the descriptor, we need to retrieve the old descriptor for comparison.
name|this
operator|.
name|unmodifiedHTableDescriptor
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
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
if|if
condition|(
name|modifiedHTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
operator|!=
name|unmodifiedHTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"REGION_REPLICATION change is not supported for enabled tables"
argument_list|)
throw|;
block|}
block|}
comment|// Find out whether all column families in unmodifiedHTableDescriptor also exists in
comment|// the modifiedHTableDescriptor. This is to determine whether we are safe to rollback.
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|oldFamilies
init|=
name|unmodifiedHTableDescriptor
operator|.
name|getFamiliesKeys
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|newFamilies
init|=
name|modifiedHTableDescriptor
operator|.
name|getFamiliesKeys
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|oldFamilies
control|)
block|{
if|if
condition|(
operator|!
name|newFamilies
operator|.
name|contains
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
name|this
operator|.
name|deleteColumnFamilyInModify
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|/**    * Action before modifying table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|preModify
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ModifyTableState
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
comment|/**    * Update descriptor    * @param env MasterProcedureEnv    * @throws IOException    **/
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
name|modifiedHTableDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Undo the descriptor change (for rollback)    * @param env MasterProcedureEnv    * @throws IOException    **/
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
name|unmodifiedHTableDescriptor
argument_list|)
expr_stmt|;
comment|// delete any new column families from the modifiedHTableDescriptor.
name|deleteFromFs
argument_list|(
name|env
argument_list|,
name|modifiedHTableDescriptor
argument_list|,
name|unmodifiedHTableDescriptor
argument_list|)
expr_stmt|;
comment|// Make sure regions are opened after table descriptor is updated.
name|reOpenAllRegionsIfTableIsOnline
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes from hdfs the families that are not longer present in the new table descriptor.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|deleteFromFs
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HTableDescriptor
name|oldHTableDescriptor
parameter_list|,
specifier|final
name|HTableDescriptor
name|newHTableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|oldFamilies
init|=
name|oldHTableDescriptor
operator|.
name|getFamiliesKeys
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|newFamilies
init|=
name|newHTableDescriptor
operator|.
name|getFamiliesKeys
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyName
range|:
name|oldFamilies
control|)
block|{
if|if
condition|(
operator|!
name|newFamilies
operator|.
name|contains
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
name|MasterDDLOperationHelper
operator|.
name|deleteColumnFamilyFromFileSystem
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getRegionInfoList
argument_list|(
name|env
argument_list|)
argument_list|,
name|familyName
argument_list|,
name|oldHTableDescriptor
operator|.
name|getFamily
argument_list|(
name|familyName
argument_list|)
operator|.
name|isMobEnabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * update replica column families if necessary.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|updateReplicaColumnsIfNeeded
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|HTableDescriptor
name|oldHTableDescriptor
parameter_list|,
specifier|final
name|HTableDescriptor
name|newHTableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|oldReplicaCount
init|=
name|oldHTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
decl_stmt|;
specifier|final
name|int
name|newReplicaCount
init|=
name|newHTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
decl_stmt|;
if|if
condition|(
name|newReplicaCount
operator|<
name|oldReplicaCount
condition|)
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|tableRows
init|=
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
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
name|scan
init|=
name|MetaTableAccessor
operator|.
name|getScanForTableName
argument_list|(
name|connection
argument_list|,
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
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
name|ResultScanner
name|resScanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|resScanner
control|)
block|{
name|tableRows
operator|.
name|add
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MetaTableAccessor
operator|.
name|removeRegionReplicasFromMeta
argument_list|(
name|tableRows
argument_list|,
name|newReplicaCount
argument_list|,
name|oldReplicaCount
operator|-
name|newReplicaCount
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Setup replication for region replicas if needed
if|if
condition|(
name|newReplicaCount
operator|>
literal|1
operator|&&
name|oldReplicaCount
operator|<=
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
block|}
comment|/**    * Action after modifying table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|postModify
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ModifyTableState
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
comment|/**    * Last action from the procedure - executed when online schema change is supported.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|reOpenAllRegionsIfTableIsOnline
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This operation only run when the table is enabled.
if|if
condition|(
operator|!
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getTableStateManager
argument_list|()
operator|.
name|isTableState
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|MasterDDLOperationHelper
operator|.
name|reOpenAllRegions
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|,
name|getRegionInfoList
argument_list|(
name|env
argument_list|)
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed modify table operation on table "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error on reopening the regions on table "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * The procedure could be restarted from a different machine. If the variable is null, we need to    * retrieve it.    * @return traceEnabled whether the trace is enabled    */
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
name|ModifyTableState
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
name|MODIFY_TABLE_PRE_OPERATION
case|:
name|cpHost
operator|.
name|preModifyTableAction
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|modifiedHTableDescriptor
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_TABLE_POST_OPERATION
case|:
name|cpHost
operator|.
name|postCompletedModifyTableAction
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|modifiedHTableDescriptor
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
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getRegionInfoList
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|regionInfoList
operator|==
literal|null
condition|)
block|{
name|regionInfoList
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
block|}
return|return
name|regionInfoList
return|;
block|}
block|}
end_class

end_unit

