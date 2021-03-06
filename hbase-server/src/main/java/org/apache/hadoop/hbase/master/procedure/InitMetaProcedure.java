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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|NamespaceDescriptor
operator|.
name|DEFAULT_NAMESPACE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|NamespaceDescriptor
operator|.
name|SYSTEM_NAMESPACE
import|;
end_import

begin_import
import|import static
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
name|TableNamespaceManager
operator|.
name|insertNamespaceToMeta
import|;
end_import

begin_import
import|import static
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
operator|.
name|AbstractStateMachineNamespaceProcedure
operator|.
name|createDirectory
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
name|concurrent
operator|.
name|CountDownLatch
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
name|RegionInfoBuilder
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
name|ProcedureUtil
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|RetryCounter
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
name|generated
operator|.
name|MasterProcedureProtos
operator|.
name|InitMetaState
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
name|InitMetaStateData
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
name|ProcedureProtos
import|;
end_import

begin_comment
comment|/**  * This procedure is used to initialize meta table for a new hbase deploy. It will just schedule an  * {@link TransitRegionStateProcedure} to assign meta.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|InitMetaProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|InitMetaState
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
name|InitMetaProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|RetryCounter
name|retryCounter
decl_stmt|;
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|TableName
operator|.
name|META_TABLE_NAME
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
name|Flow
name|executeFromState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|InitMetaState
name|state
parameter_list|)
throws|throws
name|ProcedureSuspendedException
throws|,
name|ProcedureYieldException
throws|,
name|InterruptedException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Execute {}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|INIT_META_ASSIGN_META
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to assign meta"
argument_list|)
expr_stmt|;
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createAssignProcedures
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|InitMetaState
operator|.
name|INIT_META_CREATE_NAMESPACES
argument_list|)
expr_stmt|;
return|return
name|Flow
operator|.
name|HAS_MORE_STATE
return|;
case|case
name|INIT_META_CREATE_NAMESPACES
case|:
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to create {} and {} namespaces"
argument_list|,
name|DEFAULT_NAMESPACE
argument_list|,
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
try|try
block|{
name|createDirectory
argument_list|(
name|env
argument_list|,
name|DEFAULT_NAMESPACE
argument_list|)
expr_stmt|;
name|createDirectory
argument_list|(
name|env
argument_list|,
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
comment|// here the TableNamespaceManager has not been initialized yet, so we have to insert the
comment|// record directly into meta table, later the TableNamespaceManager will load these two
comment|// namespaces when starting.
name|insertNamespaceToMeta
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|DEFAULT_NAMESPACE
argument_list|)
expr_stmt|;
name|insertNamespaceToMeta
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|SYSTEM_NAMESPACE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|retryCounter
operator|==
literal|null
condition|)
block|{
name|retryCounter
operator|=
name|ProcedureUtil
operator|.
name|createRetryCounter
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|backoff
init|=
name|retryCounter
operator|.
name|getBackoffTimeAndIncrementAttempts
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to init default and system namespaces, suspend {}secs"
argument_list|,
name|backoff
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setTimeout
argument_list|(
name|Math
operator|.
name|toIntExact
argument_list|(
name|backoff
argument_list|)
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
argument_list|)
expr_stmt|;
name|skipPersistence
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
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
literal|"unhandled state="
operator|+
name|state
argument_list|)
throw|;
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
comment|// we do not need to wait for master initialized, we are part of the initialization.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|boolean
name|setTimeoutFailure
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|setState
argument_list|(
name|ProcedureProtos
operator|.
name|ProcedureState
operator|.
name|RUNNABLE
argument_list|)
expr_stmt|;
name|env
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|addFront
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|LockState
name|acquireLock
parameter_list|(
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
annotation|@
name|Override
specifier|protected
name|void
name|rollbackState
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|InitMetaState
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
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|protected
name|InitMetaState
name|getState
parameter_list|(
name|int
name|stateId
parameter_list|)
block|{
return|return
name|InitMetaState
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
name|InitMetaState
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
name|InitMetaState
name|getInitialState
parameter_list|()
block|{
return|return
name|InitMetaState
operator|.
name|INIT_META_ASSIGN_META
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
name|serializer
operator|.
name|serialize
argument_list|(
name|InitMetaStateData
operator|.
name|getDefaultInstance
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
name|serializer
operator|.
name|deserialize
argument_list|(
name|InitMetaStateData
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|completionCleanup
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|await
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

