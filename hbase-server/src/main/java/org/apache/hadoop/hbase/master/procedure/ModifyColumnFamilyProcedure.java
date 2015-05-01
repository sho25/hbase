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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|HColumnDescriptor
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
name|executor
operator|.
name|EventType
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
name|StateMachineProcedure
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
name|ModifyColumnFamilyState
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
name|security
operator|.
name|UserGroupInformation
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
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|ModifyColumnFamilyState
argument_list|>
implements|implements
name|TableProcedureInterface
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
specifier|final
name|AtomicBoolean
name|aborted
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|HTableDescriptor
name|unmodifiedHTableDescriptor
decl_stmt|;
specifier|private
name|HColumnDescriptor
name|cfDescriptor
decl_stmt|;
specifier|private
name|UserGroupInformation
name|user
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
decl_stmt|;
specifier|public
name|ModifyColumnFamilyProcedure
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
name|HColumnDescriptor
name|cfDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
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
name|user
operator|=
name|env
operator|.
name|getRequestUser
argument_list|()
operator|.
name|getUGI
argument_list|()
expr_stmt|;
name|this
operator|.
name|unmodifiedHTableDescriptor
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
name|InterruptedException
decl||
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error trying to modify the column family "
operator|+
name|getColumnFamilyName
argument_list|()
operator|+
literal|" of the table "
operator|+
name|tableName
operator|+
literal|"(in state="
operator|+
name|state
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-modify-columnfamily"
argument_list|,
name|e
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
literal|" rollback state="
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
name|MODIFY_COLUMN_FAMILY_REOPEN_ALL_REGIONS
case|:
break|break;
comment|// Nothing to undo.
case|case
name|MODIFY_COLUMN_FAMILY_POST_OPERATION
case|:
comment|// TODO-MAYBE: call the coprocessor event to undo?
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
case|:
name|restoreTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_PRE_OPERATION
case|:
comment|// TODO-MAYBE: call the coprocessor event to undo?
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_PREPARE
case|:
break|break;
comment|// nothing to do
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
comment|// This will be retried. Unless there is a bug in the code,
comment|// this should be just a "temporary error" (e.g. network down)
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed rollback attempt step "
operator|+
name|state
operator|+
literal|" for adding the column family"
operator|+
name|getColumnFamilyName
argument_list|()
operator|+
literal|" to the table "
operator|+
name|tableName
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
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
name|setNextState
parameter_list|(
name|ModifyColumnFamilyState
name|state
parameter_list|)
block|{
if|if
condition|(
name|aborted
operator|.
name|get
argument_list|()
condition|)
block|{
name|setAbortFailure
argument_list|(
literal|"modify-columnfamily"
argument_list|,
literal|"abort requested"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|super
operator|.
name|setNextState
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
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
name|aborted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|acquireLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
if|if
condition|(
operator|!
name|env
operator|.
name|isInitialized
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|tryAcquireTableWrite
argument_list|(
name|tableName
argument_list|,
name|EventType
operator|.
name|C_M_MODIFY_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|releaseLock
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|env
operator|.
name|getProcedureQueue
argument_list|()
operator|.
name|releaseTableWrite
argument_list|(
name|tableName
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
name|user
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
name|cfDescriptor
operator|.
name|convert
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|unmodifiedHTableDescriptor
operator|!=
literal|null
condition|)
block|{
name|modifyCFMsg
operator|.
name|setUnmodifiedTableSchema
argument_list|(
name|unmodifiedHTableDescriptor
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|modifyCFMsg
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
name|ModifyColumnFamilyStateData
name|modifyCFMsg
init|=
name|MasterProcedureProtos
operator|.
name|ModifyColumnFamilyStateData
operator|.
name|parseDelimitedFrom
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|user
operator|=
name|MasterProcedureUtil
operator|.
name|toUserInfo
argument_list|(
name|modifyCFMsg
operator|.
name|getUserInfo
argument_list|()
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
name|HColumnDescriptor
operator|.
name|convert
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
name|unmodifiedHTableDescriptor
operator|=
name|HTableDescriptor
operator|.
name|convert
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
literal|") user="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|user
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
name|MasterDDLOperationHelper
operator|.
name|checkTableModifiable
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
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
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|unmodifiedHTableDescriptor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"HTableDescriptor missing for "
operator|+
name|tableName
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|unmodifiedHTableDescriptor
operator|.
name|hasFamily
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
name|HTableDescriptor
name|htd
init|=
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
decl_stmt|;
name|htd
operator|.
name|modifyFamily
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
name|htd
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
name|getAssignmentManager
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
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionInfoList
init|=
name|ProcedureSyncWait
operator|.
name|getRegionsFromMeta
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
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
name|regionInfoList
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed add column family operation on table "
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
name|user
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
throws|throws
name|Exception
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
name|preModifyColumnHandler
argument_list|(
name|tableName
argument_list|,
name|cfDescriptor
argument_list|)
expr_stmt|;
break|break;
case|case
name|MODIFY_COLUMN_FAMILY_POST_OPERATION
case|:
name|cpHost
operator|.
name|postModifyColumnHandler
argument_list|(
name|tableName
argument_list|,
name|cfDescriptor
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
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

