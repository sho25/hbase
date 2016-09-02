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
name|HashMap
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
name|Map
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
name|ServerName
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
name|master
operator|.
name|AssignmentManager
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
name|BulkAssigner
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
name|GeneralBulkAssigner
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
name|MasterServices
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
name|RegionStates
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
name|ServerManager
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
name|EnableTableState
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
name|security
operator|.
name|User
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
name|Pair
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
name|zookeeper
operator|.
name|MetaTableLocator
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EnableTableProcedure
extends|extends
name|StateMachineProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|EnableTableState
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
name|EnableTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// This is for back compatible with 1.0 asynchronized operations.
specifier|private
specifier|final
name|ProcedurePrepareLatch
name|syncLatch
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
name|User
name|user
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
init|=
literal|null
decl_stmt|;
specifier|public
name|EnableTableProcedure
parameter_list|()
block|{
name|syncLatch
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableName the table to operate on    * @param skipTableStateCheck whether to check table state    */
specifier|public
name|EnableTableProcedure
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
name|EnableTableProcedure
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
name|this
operator|.
name|user
operator|=
name|env
operator|.
name|getRequestUser
argument_list|()
expr_stmt|;
name|this
operator|.
name|setOwner
argument_list|(
name|this
operator|.
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Compatible with 1.0: We use latch to make sure that this procedure implementation is
comment|// compatible with 1.0 asynchronized operations. We need to lock the table and check
comment|// whether the Enable operation could be performed (table exists and offline; table state
comment|// is DISABLED). Once it is done, we are good to release the latch and the client can
comment|// start asynchronously wait for the operation.
comment|//
comment|// Note: the member syncLatch could be null if we are in failover or recovery scenario.
comment|// This is ok for backward compatible, as 1.0 client would not able to peek at procedure.
name|this
operator|.
name|syncLatch
operator|=
name|syncLatch
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
name|EnableTableState
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
name|ENABLE_TABLE_PREPARE
case|:
if|if
condition|(
name|prepareEnable
argument_list|(
name|env
argument_list|)
condition|)
block|{
name|setNextState
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_PRE_OPERATION
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|isFailed
argument_list|()
operator|:
literal|"enable should have an exception here"
assert|;
return|return
name|Flow
operator|.
name|NO_MORE_STATE
return|;
block|}
break|break;
case|case
name|ENABLE_TABLE_PRE_OPERATION
case|:
name|preEnable
argument_list|(
name|env
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_SET_ENABLING_TABLE_STATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|ENABLE_TABLE_SET_ENABLING_TABLE_STATE
case|:
name|setTableStateToEnabling
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_MARK_REGIONS_ONLINE
argument_list|)
expr_stmt|;
break|break;
case|case
name|ENABLE_TABLE_MARK_REGIONS_ONLINE
case|:
name|markRegionsOnline
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_SET_ENABLED_TABLE_STATE
argument_list|)
expr_stmt|;
break|break;
case|case
name|ENABLE_TABLE_SET_ENABLED_TABLE_STATE
case|:
name|setTableStateToEnabled
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|EnableTableState
operator|.
name|ENABLE_TABLE_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|ENABLE_TABLE_POST_OPERATION
case|:
name|postEnable
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
literal|"master-enable-table"
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
literal|"Retriable error trying to enable table="
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
name|EnableTableState
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
name|ENABLE_TABLE_PRE_OPERATION
case|:
return|return;
case|case
name|ENABLE_TABLE_PREPARE
case|:
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|syncLatch
argument_list|,
name|this
argument_list|)
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
name|EnableTableState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|ENABLE_TABLE_PREPARE
case|:
case|case
name|ENABLE_TABLE_PRE_OPERATION
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
name|EnableTableState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|EnableTableState
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
name|EnableTableState
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
name|EnableTableState
name|getInitialState
parameter_list|()
block|{
return|return
name|EnableTableState
operator|.
name|ENABLE_TABLE_PREPARE
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
name|env
operator|.
name|waitInitialized
argument_list|(
name|this
argument_list|)
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
name|this
argument_list|,
name|tableName
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
name|this
argument_list|,
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
name|EnableTableStateData
operator|.
name|Builder
name|enableTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|EnableTableStateData
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
name|setSkipTableStateCheck
argument_list|(
name|skipTableStateCheck
argument_list|)
decl_stmt|;
name|enableTableMsg
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
name|EnableTableStateData
name|enableTableMsg
init|=
name|MasterProcedureProtos
operator|.
name|EnableTableStateData
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
name|enableTableMsg
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
name|enableTableMsg
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|skipTableStateCheck
operator|=
name|enableTableMsg
operator|.
name|getSkipTableStateCheck
argument_list|()
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
name|ENABLE
return|;
block|}
comment|/**    * Action before any real action of enabling table. Set the exception in the procedure instead    * of throwing it.  This approach is to deal with backward compatible with 1.0.    * @param env MasterProcedureEnv    * @return whether the table passes the necessary checks    * @throws IOException    */
specifier|private
name|boolean
name|prepareEnable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|canTableBeEnabled
init|=
literal|true
decl_stmt|;
comment|// Check whether table exists
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
literal|"master-enable-table"
argument_list|,
operator|new
name|TableNotFoundException
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|canTableBeEnabled
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
comment|// the state to ENABLING from DISABLED. The implementation was done before table lock
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
name|DISABLED
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
literal|" isn't disabled;is "
operator|+
name|state
operator|.
name|name
argument_list|()
operator|+
literal|"; skipping enable"
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-enable-table"
argument_list|,
operator|new
name|TableNotDisabledException
argument_list|(
name|this
operator|.
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
name|canTableBeEnabled
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// We are done the check. Future actions in this procedure could be done asynchronously.
name|ProcedurePrepareLatch
operator|.
name|releaseLatch
argument_list|(
name|syncLatch
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return
name|canTableBeEnabled
return|;
block|}
comment|/**    * Action before enabling table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|preEnable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|EnableTableState
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
comment|/**    * Mark table state to Enabling    * @param env MasterProcedureEnv    * @param tableName the target table    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|setTableStateToEnabling
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to enable the table "
operator|+
name|tableName
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
comment|/**    * Mark offline regions of the table online with retry    * @param env MasterProcedureEnv    * @param tableName the target table    * @param retryRequired whether to retry if the first run failed    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|markRegionsOnline
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
name|Boolean
name|retryRequired
parameter_list|)
throws|throws
name|IOException
block|{
comment|// This is best effort approach to make all regions of a table online.  If we fail to do
comment|// that, it is ok that the table has some offline regions; user can fix it manually.
comment|// Dev consideration: add a config to control max number of retry. For now, it is hard coded.
name|int
name|maxTry
init|=
operator|(
name|retryRequired
condition|?
literal|10
else|:
literal|1
operator|)
decl_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
do|do
block|{
try|try
block|{
name|done
operator|=
name|markRegionsOnline
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|done
condition|)
block|{
break|break;
block|}
name|maxTry
operator|--
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Received exception while marking regions online. tries left: "
operator|+
name|maxTry
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|maxTry
operator|--
expr_stmt|;
if|if
condition|(
name|maxTry
operator|>
literal|0
condition|)
block|{
continue|continue;
comment|// we still have some retry left, try again.
block|}
throw|throw
name|e
throw|;
block|}
block|}
do|while
condition|(
name|maxTry
operator|>
literal|0
condition|)
do|;
if|if
condition|(
operator|!
name|done
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Some or all regions of the Table '"
operator|+
name|tableName
operator|+
literal|"' were offline"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Mark offline regions of the table online    * @param env MasterProcedureEnv    * @param tableName the target table    * @return whether the operation is fully completed or being interrupted.    * @throws IOException    */
specifier|private
specifier|static
name|boolean
name|markRegionsOnline
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
specifier|final
name|AssignmentManager
name|assignmentManager
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
specifier|final
name|MasterServices
name|masterServices
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
decl_stmt|;
specifier|final
name|ServerManager
name|serverManager
init|=
name|masterServices
operator|.
name|getServerManager
argument_list|()
decl_stmt|;
name|boolean
name|done
init|=
literal|false
decl_stmt|;
comment|// Get the regions of this table. We're done when all listed
comment|// tables are onlined.
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|tableRegionsAndLocations
decl_stmt|;
if|if
condition|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|tableRegionsAndLocations
operator|=
operator|new
name|MetaTableLocator
argument_list|()
operator|.
name|getMetaRegionsAndLocations
argument_list|(
name|masterServices
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableRegionsAndLocations
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|masterServices
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
name|int
name|countOfRegionsInTable
init|=
name|tableRegionsAndLocations
operator|.
name|size
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionsToAssign
init|=
name|regionsToAssignWithServerName
argument_list|(
name|env
argument_list|,
name|tableRegionsAndLocations
argument_list|)
decl_stmt|;
comment|// need to potentially create some regions for the replicas
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|unrecordedReplicas
init|=
name|AssignmentManager
operator|.
name|replicaRegionsNotRecordedInMeta
argument_list|(
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|(
name|regionsToAssign
operator|.
name|keySet
argument_list|()
argument_list|)
argument_list|,
name|masterServices
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|srvToUnassignedRegs
init|=
name|assignmentManager
operator|.
name|getBalancer
argument_list|()
operator|.
name|roundRobinAssignment
argument_list|(
name|unrecordedReplicas
argument_list|,
name|serverManager
operator|.
name|getOnlineServersList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|srvToUnassignedRegs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|entry
range|:
name|srvToUnassignedRegs
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|HRegionInfo
name|h
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|regionsToAssign
operator|.
name|put
argument_list|(
name|h
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|int
name|offlineRegionsCount
init|=
name|regionsToAssign
operator|.
name|size
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table '"
operator|+
name|tableName
operator|+
literal|"' has "
operator|+
name|countOfRegionsInTable
operator|+
literal|" regions, of which "
operator|+
name|offlineRegionsCount
operator|+
literal|" are offline."
argument_list|)
expr_stmt|;
if|if
condition|(
name|offlineRegionsCount
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
name|List
argument_list|<
name|ServerName
argument_list|>
name|onlineServers
init|=
name|serverManager
operator|.
name|createDestinationServersList
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|bulkPlan
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getBalancer
argument_list|()
operator|.
name|retainAssignment
argument_list|(
name|regionsToAssign
argument_list|,
name|onlineServers
argument_list|)
decl_stmt|;
if|if
condition|(
name|bulkPlan
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Bulk assigning "
operator|+
name|offlineRegionsCount
operator|+
literal|" region(s) across "
operator|+
name|bulkPlan
operator|.
name|size
argument_list|()
operator|+
literal|" server(s), retainAssignment=true"
argument_list|)
expr_stmt|;
name|BulkAssigner
name|ba
init|=
operator|new
name|GeneralBulkAssigner
argument_list|(
name|masterServices
argument_list|,
name|bulkPlan
argument_list|,
name|assignmentManager
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ba
operator|.
name|bulkAssign
argument_list|()
condition|)
block|{
name|done
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Enable operation was interrupted when enabling table '"
operator|+
name|tableName
operator|+
literal|"'"
argument_list|)
expr_stmt|;
comment|// Preserve the interrupt.
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Balancer was unable to find suitable servers for table "
operator|+
name|tableName
operator|+
literal|", leaving unassigned"
argument_list|)
expr_stmt|;
block|}
return|return
name|done
return|;
block|}
comment|/**    * Mark regions of the table offline during recovery    * @param env MasterProcedureEnv    */
specifier|private
name|void
name|markRegionsOfflineDuringRecovery
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
try|try
block|{
comment|// This is a best effort attempt. We will move on even it does not succeed. We will retry
comment|// several times until we giving up.
name|DisableTableProcedure
operator|.
name|markRegionsOffline
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to offline all regions of table "
operator|+
name|tableName
operator|+
literal|". Ignoring"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Mark table state to Enabled    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|protected
specifier|static
name|void
name|setTableStateToEnabled
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
comment|// Flip the table to Enabled
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Table '"
operator|+
name|tableName
operator|+
literal|"' was successfully enabled."
argument_list|)
expr_stmt|;
block|}
comment|/**    * Action after enabling table.    * @param env MasterProcedureEnv    * @param state the procedure state    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|postEnable
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|EnableTableState
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
comment|/**    * @param regionsInMeta    * @return List of regions neither in transition nor assigned.    * @throws IOException    */
specifier|private
specifier|static
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionsToAssignWithServerName
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|regionsInMeta
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionsToAssign
init|=
operator|new
name|HashMap
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|(
name|regionsInMeta
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|RegionStates
name|regionStates
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|regionLocation
range|:
name|regionsInMeta
control|)
block|{
name|HRegionInfo
name|hri
init|=
name|regionLocation
operator|.
name|getFirst
argument_list|()
decl_stmt|;
name|ServerName
name|sn
init|=
name|regionLocation
operator|.
name|getSecond
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionStates
operator|.
name|isRegionOffline
argument_list|(
name|hri
argument_list|)
condition|)
block|{
name|regionsToAssign
operator|.
name|put
argument_list|(
name|hri
argument_list|,
name|sn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping assign for the region "
operator|+
name|hri
operator|+
literal|" during enable table "
operator|+
name|hri
operator|.
name|getTable
argument_list|()
operator|+
literal|" because its already in tranition or assigned."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|regionsToAssign
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
name|EnableTableState
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
name|ENABLE_TABLE_PRE_OPERATION
case|:
name|cpHost
operator|.
name|preEnableTableAction
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|user
argument_list|)
expr_stmt|;
break|break;
case|case
name|ENABLE_TABLE_POST_OPERATION
case|:
name|cpHost
operator|.
name|postCompletedEnableTableAction
argument_list|(
name|getTableName
argument_list|()
argument_list|,
name|user
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

