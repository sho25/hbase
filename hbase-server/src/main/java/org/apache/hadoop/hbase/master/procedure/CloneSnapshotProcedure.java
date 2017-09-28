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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|conf
operator|.
name|Configuration
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|master
operator|.
name|MetricsSnapshot
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
name|procedure
operator|.
name|CreateTableProcedure
operator|.
name|CreateHdfsRegions
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
name|monitoring
operator|.
name|MonitoredTask
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
name|monitoring
operator|.
name|TaskMonitor
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
name|snapshot
operator|.
name|ClientSnapshotDescriptionUtils
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
name|snapshot
operator|.
name|RestoreSnapshotException
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
name|snapshot
operator|.
name|RestoreSnapshotHelper
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotManifest
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
name|Pair
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|CloneSnapshotState
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
name|SnapshotProtos
operator|.
name|SnapshotDescription
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|CloneSnapshotProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|CloneSnapshotState
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
name|CloneSnapshotProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableDescriptor
name|tableDescriptor
decl_stmt|;
specifier|private
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
name|boolean
name|restoreAcl
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|newRegions
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|parentsToChildrenPairMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Monitor
specifier|private
name|MonitoredTask
name|monitorStatus
init|=
literal|null
decl_stmt|;
specifier|private
name|Boolean
name|traceEnabled
init|=
literal|null
decl_stmt|;
comment|/**    * Constructor (for failover)    */
specifier|public
name|CloneSnapshotProcedure
parameter_list|()
block|{   }
specifier|public
name|CloneSnapshotProcedure
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
name|SnapshotDescription
name|snapshot
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|snapshot
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableDescriptor the table to operate on    * @param snapshot snapshot to clone from    */
specifier|public
name|CloneSnapshotProcedure
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
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|boolean
name|restoreAcl
parameter_list|)
block|{
name|super
argument_list|(
name|env
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
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|restoreAcl
operator|=
name|restoreAcl
expr_stmt|;
name|getMonitorStatus
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set up monitor status if it is not created.    */
specifier|private
name|MonitoredTask
name|getMonitorStatus
parameter_list|()
block|{
if|if
condition|(
name|monitorStatus
operator|==
literal|null
condition|)
block|{
name|monitorStatus
operator|=
name|TaskMonitor
operator|.
name|get
argument_list|()
operator|.
name|createStatus
argument_list|(
literal|"Cloning  snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"' to table "
operator|+
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|monitorStatus
return|;
block|}
specifier|private
name|void
name|restoreSnapshotAcl
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
if|if
condition|(
name|restoreAcl
operator|&&
name|snapshot
operator|.
name|hasUsersAndPermissions
argument_list|()
operator|&&
name|snapshot
operator|.
name|getUsersAndPermissions
argument_list|()
operator|!=
literal|null
operator|&&
name|SnapshotDescriptionUtils
operator|.
name|isSecurityAvailable
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|RestoreSnapshotHelper
operator|.
name|restoreSnapshotAcl
argument_list|(
name|snapshot
argument_list|,
name|tableDescriptor
operator|.
name|getTableName
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|CloneSnapshotState
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
name|CLONE_SNAPSHOT_PRE_OPERATION
case|:
comment|// Verify if we can clone the table
name|prepareClone
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|preCloneSnapshot
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_WRITE_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPSHOT_WRITE_FS_LAYOUT
case|:
name|newRegions
operator|=
name|createFilesystemLayout
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
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_ADD_TO_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPSHOT_ADD_TO_META
case|:
name|addRegionsToMeta
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_ASSIGN_REGIONS
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPSHOT_ASSIGN_REGIONS
case|:
name|CreateTableProcedure
operator|.
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
name|createAssignProcedures
argument_list|(
name|newRegions
argument_list|)
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_UPDATE_DESC_CACHE
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPSHOT_UPDATE_DESC_CACHE
case|:
name|CreateTableProcedure
operator|.
name|setEnabledState
argument_list|(
name|env
argument_list|,
name|getTableName
argument_list|()
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
name|CloneSnapshotState
operator|.
name|CLONE_SNAPHOST_RESTORE_ACL
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPHOST_RESTORE_ACL
case|:
name|restoreSnapshotAcl
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_POST_OPERATION
argument_list|)
expr_stmt|;
break|break;
case|case
name|CLONE_SNAPSHOT_POST_OPERATION
case|:
name|postCloneSnapshot
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|MetricsSnapshot
name|metricsSnapshot
init|=
operator|new
name|MetricsSnapshot
argument_list|()
decl_stmt|;
name|metricsSnapshot
operator|.
name|addSnapshotClone
argument_list|(
name|getMonitorStatus
argument_list|()
operator|.
name|getCompletionTimestamp
argument_list|()
operator|-
name|getMonitorStatus
argument_list|()
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|getMonitorStatus
argument_list|()
operator|.
name|markComplete
argument_list|(
literal|"Clone snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"' completed!"
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
literal|"master-clone-snapshot"
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
literal|"Retriable error trying to clone snapshot="
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" to table="
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
name|CloneSnapshotState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_PRE_OPERATION
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
comment|// TODO-MAYBE: call the deleteTable coprocessor event?
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
name|CloneSnapshotState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|CLONE_SNAPSHOT_PRE_OPERATION
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
name|CloneSnapshotState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|CloneSnapshotState
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
name|CloneSnapshotState
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
name|CloneSnapshotState
name|getInitialState
parameter_list|()
block|{
return|return
name|CloneSnapshotState
operator|.
name|CLONE_SNAPSHOT_PRE_OPERATION
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
comment|// Clone is creating a table
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
literal|" snapshot="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|snapshot
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
name|CloneSnapshotStateData
operator|.
name|Builder
name|cloneSnapshotMsg
init|=
name|MasterProcedureProtos
operator|.
name|CloneSnapshotStateData
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
name|setSnapshot
argument_list|(
name|this
operator|.
name|snapshot
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
name|cloneSnapshotMsg
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
if|if
condition|(
operator|!
name|parentsToChildrenPairMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|>
name|it
init|=
name|parentsToChildrenPairMap
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|entry
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|MasterProcedureProtos
operator|.
name|RestoreParentToChildRegionsPair
operator|.
name|Builder
name|parentToChildrenPair
init|=
name|MasterProcedureProtos
operator|.
name|RestoreParentToChildRegionsPair
operator|.
name|newBuilder
argument_list|()
operator|.
name|setParentRegionName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|setChild1RegionName
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getFirst
argument_list|()
argument_list|)
operator|.
name|setChild2RegionName
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
name|cloneSnapshotMsg
operator|.
name|addParentToChildRegionsPairList
argument_list|(
name|parentToChildrenPair
argument_list|)
expr_stmt|;
block|}
block|}
name|serializer
operator|.
name|serialize
argument_list|(
name|cloneSnapshotMsg
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
name|CloneSnapshotStateData
name|cloneSnapshotMsg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|CloneSnapshotStateData
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
name|cloneSnapshotMsg
operator|.
name|getUserInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|cloneSnapshotMsg
operator|.
name|getSnapshot
argument_list|()
expr_stmt|;
name|tableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toTableDescriptor
argument_list|(
name|cloneSnapshotMsg
operator|.
name|getTableSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cloneSnapshotMsg
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
name|cloneSnapshotMsg
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
name|cloneSnapshotMsg
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
if|if
condition|(
name|cloneSnapshotMsg
operator|.
name|getParentToChildRegionsPairListCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|parentsToChildrenPairMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|MasterProcedureProtos
operator|.
name|RestoreParentToChildRegionsPair
name|parentToChildrenPair
range|:
name|cloneSnapshotMsg
operator|.
name|getParentToChildRegionsPairListList
argument_list|()
control|)
block|{
name|parentsToChildrenPairMap
operator|.
name|put
argument_list|(
name|parentToChildrenPair
operator|.
name|getParentRegionName
argument_list|()
argument_list|,
operator|new
name|Pair
argument_list|<>
argument_list|(
name|parentToChildrenPair
operator|.
name|getChild1RegionName
argument_list|()
argument_list|,
name|parentToChildrenPair
operator|.
name|getChild2RegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Make sure that the monitor status is set up
name|getMonitorStatus
argument_list|()
expr_stmt|;
block|}
comment|/**    * Action before any real action of cloning from snapshot.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareClone
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
throw|throw
operator|new
name|TableExistsException
argument_list|(
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Action before cloning from snapshot.    * @param env MasterProcedureEnv    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|preCloneSnapshot
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
comment|// Check and update namespace quota
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
name|SnapshotManifest
name|manifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|env
operator|.
name|getMasterConfiguration
argument_list|()
argument_list|,
name|mfs
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|)
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
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
name|manifest
operator|.
name|getRegionManifestsMap
argument_list|()
operator|.
name|size
argument_list|()
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
name|cpHost
operator|.
name|preCreateTableAction
argument_list|(
name|tableDescriptor
argument_list|,
literal|null
argument_list|,
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Action after cloning from snapshot.    * @param env MasterProcedureEnv    * @throws IOException    * @throws InterruptedException    */
specifier|private
name|void
name|postCloneSnapshot
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
comment|/**    * Create regions in file system.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|createFilesystemLayout
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
name|rootDir
init|=
name|mfs
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
name|env
operator|.
name|getMasterConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|ForeignExceptionDispatcher
name|monitorException
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
name|getMonitorStatus
argument_list|()
operator|.
name|setStatus
argument_list|(
literal|"Clone snapshot - creating regions for table: "
operator|+
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
comment|// 1. Execute the on-disk Clone
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
name|RestoreSnapshotHelper
name|restoreHelper
init|=
operator|new
name|RestoreSnapshotHelper
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|,
name|tableDescriptor
argument_list|,
name|tableRootDir
argument_list|,
name|monitorException
argument_list|,
name|monitorStatus
argument_list|)
decl_stmt|;
name|RestoreSnapshotHelper
operator|.
name|RestoreMetaChanges
name|metaChanges
init|=
name|restoreHelper
operator|.
name|restoreHdfsRegions
argument_list|()
decl_stmt|;
comment|// Clone operation should not have stuff to restore or remove
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|metaChanges
operator|.
name|hasRegionsToRestore
argument_list|()
argument_list|,
literal|"A clone should not have regions to restore"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|metaChanges
operator|.
name|hasRegionsToRemove
argument_list|()
argument_list|,
literal|"A clone should not have regions to remove"
argument_list|)
expr_stmt|;
comment|// At this point the clone is complete. Next step is enabling the table.
name|String
name|msg
init|=
literal|"Clone snapshot="
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|" on table="
operator|+
name|tableName
operator|+
literal|" completed!"
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|monitorStatus
operator|.
name|setStatus
argument_list|(
name|msg
operator|+
literal|" Waiting for table to be enabled..."
argument_list|)
expr_stmt|;
comment|// 2. Let the next step to add the regions to meta
return|return
name|metaChanges
operator|.
name|getRegionsToAdd
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"clone snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" failed because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|IOException
name|rse
init|=
operator|new
name|RestoreSnapshotException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|,
name|ProtobufUtil
operator|.
name|createSnapshotDesc
argument_list|(
name|snapshot
argument_list|)
argument_list|)
decl_stmt|;
comment|// these handlers aren't futures so we need to register the error here.
name|monitorException
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
literal|"Master CloneSnapshotProcedure"
argument_list|,
name|rse
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|rse
throw|;
block|}
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * Create region layout in file system.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
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
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableDescriptor
argument_list|)
operator|.
name|build
argument_list|()
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
name|CreateTableProcedure
operator|.
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
comment|/**    * Add regions to hbase:meta table.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|addRegionsToMeta
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|newRegions
operator|=
name|CreateTableProcedure
operator|.
name|addTableToMeta
argument_list|(
name|env
argument_list|,
name|tableDescriptor
argument_list|,
name|newRegions
argument_list|)
expr_stmt|;
name|RestoreSnapshotHelper
operator|.
name|RestoreMetaChanges
name|metaChanges
init|=
operator|new
name|RestoreSnapshotHelper
operator|.
name|RestoreMetaChanges
argument_list|(
name|tableDescriptor
argument_list|,
name|parentsToChildrenPairMap
argument_list|)
decl_stmt|;
name|metaChanges
operator|.
name|updateMetaParentRegions
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
argument_list|,
name|newRegions
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
block|}
end_class

end_unit

