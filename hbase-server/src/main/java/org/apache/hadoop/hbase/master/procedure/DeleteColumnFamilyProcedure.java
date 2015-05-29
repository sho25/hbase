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
name|DeleteColumnFamilyState
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
name|ByteStringer
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
name|Bytes
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
comment|/**  * The procedure to delete a column family from an existing table.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|DeleteColumnFamilyProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|DeleteColumnFamilyState
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
name|DeleteColumnFamilyProcedure
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
name|HTableDescriptor
name|unmodifiedHTableDescriptor
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|familyName
decl_stmt|;
specifier|private
name|UserGroupInformation
name|user
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
decl_stmt|;
specifier|public
name|DeleteColumnFamilyProcedure
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
block|}
specifier|public
name|DeleteColumnFamilyProcedure
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
name|byte
index|[]
name|familyName
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
name|familyName
operator|=
name|familyName
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
name|DeleteColumnFamilyState
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
name|DELETE_COLUMN_FAMILY_PREPARE
case|:
name|prepareDelete
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_PRE_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_PRE_OPERATION
case|:
name|preDelete
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
case|:
name|updateTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_DELETE_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_DELETE_FS_LAYOUT
case|:
name|deleteFromFs
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_POST_OPERATION
case|:
name|postDelete
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_REOPEN_ALL_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_REOPEN_ALL_REGIONS
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
if|if
condition|(
operator|!
name|isRollbackSupported
argument_list|(
name|state
argument_list|)
condition|)
block|{
comment|// We reach a state that cannot be rolled back. We just need to keep retry.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error trying to delete the column family "
operator|+
name|getColumnFamilyName
argument_list|()
operator|+
literal|" from table "
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
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error trying to delete the column family "
operator|+
name|getColumnFamilyName
argument_list|()
operator|+
literal|" from table "
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
literal|"master-delete-column-family"
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
name|DeleteColumnFamilyState
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
name|DELETE_COLUMN_FAMILY_REOPEN_ALL_REGIONS
case|:
break|break;
comment|// Nothing to undo.
case|case
name|DELETE_COLUMN_FAMILY_POST_OPERATION
case|:
comment|// TODO-MAYBE: call the coprocessor event to undo?
break|break;
case|case
name|DELETE_COLUMN_FAMILY_DELETE_FS_LAYOUT
case|:
comment|// Once we reach to this state - we could NOT rollback - as it is tricky to undelete
comment|// the deleted files. We are not suppose to reach here, throw exception so that we know
comment|// there is a code bug to investigate.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|this
operator|+
literal|" rollback of state="
operator|+
name|state
operator|+
literal|" is unsupported."
argument_list|)
throw|;
case|case
name|DELETE_COLUMN_FAMILY_UPDATE_TABLE_DESCRIPTOR
case|:
name|restoreTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_PRE_OPERATION
case|:
comment|// TODO-MAYBE: call the coprocessor event to undo?
break|break;
case|case
name|DELETE_COLUMN_FAMILY_PREPARE
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
literal|" for deleting the column family"
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
name|DeleteColumnFamilyState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|DeleteColumnFamilyState
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
name|DeleteColumnFamilyState
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
name|DeleteColumnFamilyState
name|getInitialState
parameter_list|()
block|{
return|return
name|DeleteColumnFamilyState
operator|.
name|DELETE_COLUMN_FAMILY_PREPARE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setNextState
parameter_list|(
name|DeleteColumnFamilyState
name|state
parameter_list|)
block|{
if|if
condition|(
name|aborted
operator|.
name|get
argument_list|()
operator|&&
name|isRollbackSupported
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|setAbortFailure
argument_list|(
literal|"delete-columnfamily"
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
name|tryAcquireTableExclusiveLock
argument_list|(
name|tableName
argument_list|,
name|EventType
operator|.
name|C_M_DELETE_FAMILY
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
name|releaseTableExclusiveLock
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
name|DeleteColumnFamilyStateData
operator|.
name|Builder
name|deleteCFMsg
init|=
name|MasterProcedureProtos
operator|.
name|DeleteColumnFamilyStateData
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
name|setColumnfamilyName
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|familyName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|unmodifiedHTableDescriptor
operator|!=
literal|null
condition|)
block|{
name|deleteCFMsg
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
name|deleteCFMsg
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
name|DeleteColumnFamilyStateData
name|deleteCFMsg
init|=
name|MasterProcedureProtos
operator|.
name|DeleteColumnFamilyStateData
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
name|deleteCFMsg
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
name|deleteCFMsg
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|familyName
operator|=
name|deleteCFMsg
operator|.
name|getColumnfamilyName
argument_list|()
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|deleteCFMsg
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
name|deleteCFMsg
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
name|familyName
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
comment|/**    * Action before any real action of deleting column family.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareDelete
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
comment|// In order to update the descriptor, we need to retrieve the old descriptor for comparison.
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
name|familyName
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
literal|"' does not exist, so it cannot be deleted"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Action before deleting column family.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|preDelete
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|DeleteColumnFamilyState
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
comment|/**    * Remove the column family from the file system and update the table descriptor    */
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
literal|"DeleteColumn. Table = "
operator|+
name|tableName
operator|+
literal|" family = "
operator|+
name|getColumnFamilyName
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
if|if
condition|(
operator|!
name|htd
operator|.
name|hasFamily
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
comment|// It is possible to reach this situation, as we could already delete the column family
comment|// from table descriptor, but the master failover happens before we complete this state.
comment|// We should be able to handle running this function multiple times without causing problem.
return|return;
block|}
name|htd
operator|.
name|removeFamily
argument_list|(
name|familyName
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
comment|/**    * Remove the column family from the file system    **/
specifier|private
name|void
name|deleteFromFs
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterDDLOperationHelper
operator|.
name|deleteColumnFamilyFromFileSystem
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
name|getRegionInfoList
argument_list|(
name|env
argument_list|)
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
block|}
comment|/**    * Action after deleting column family.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|postDelete
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|DeleteColumnFamilyState
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
literal|"Completed delete column family operation on table "
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
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
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
name|DeleteColumnFamilyState
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
name|DELETE_COLUMN_FAMILY_PRE_OPERATION
case|:
name|cpHost
operator|.
name|preDeleteColumnHandler
argument_list|(
name|tableName
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE_COLUMN_FAMILY_POST_OPERATION
case|:
name|cpHost
operator|.
name|postDeleteColumnHandler
argument_list|(
name|tableName
argument_list|,
name|familyName
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
comment|/*    * Check whether we are in the state that can be rollback    */
specifier|private
name|boolean
name|isRollbackSupported
parameter_list|(
specifier|final
name|DeleteColumnFamilyState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|DELETE_COLUMN_FAMILY_REOPEN_ALL_REGIONS
case|:
case|case
name|DELETE_COLUMN_FAMILY_POST_OPERATION
case|:
case|case
name|DELETE_COLUMN_FAMILY_DELETE_FS_LAYOUT
case|:
comment|// It is not safe to rollback if we reach to these states.
return|return
literal|false
return|;
default|default:
break|break;
block|}
return|return
literal|true
return|;
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

