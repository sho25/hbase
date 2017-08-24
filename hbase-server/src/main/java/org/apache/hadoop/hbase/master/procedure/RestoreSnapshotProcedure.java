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
name|RestoreSnapshotState
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
name|Pair
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RestoreSnapshotProcedure
extends|extends
name|AbstractStateMachineTableProcedure
argument_list|<
name|RestoreSnapshotState
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
name|RestoreSnapshotProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableDescriptor
name|modifiedTableDescriptor
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToRestore
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToRemove
init|=
literal|null
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regionsToAdd
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
specifier|private
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
name|boolean
name|restoreAcl
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
name|RestoreSnapshotProcedure
parameter_list|()
block|{   }
specifier|public
name|RestoreSnapshotProcedure
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
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableDescriptor the table to operate on    * @param snapshot snapshot to restore from    * @throws IOException    */
specifier|public
name|RestoreSnapshotProcedure
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
comment|// This is the new schema we are going to write out as this modification.
name|this
operator|.
name|modifiedTableDescriptor
operator|=
name|tableDescriptor
expr_stmt|;
comment|// Snapshot information
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
comment|// Monitor
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
literal|"Restoring  snapshot '"
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
name|RestoreSnapshotState
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
comment|// Make sure that the monitor status is set up
name|getMonitorStatus
argument_list|()
expr_stmt|;
try|try
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|RESTORE_SNAPSHOT_PRE_OPERATION
case|:
comment|// Verify if we can restore the table
name|prepareRestore
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_UPDATE_TABLE_DESCRIPTOR
argument_list|)
expr_stmt|;
break|break;
case|case
name|RESTORE_SNAPSHOT_UPDATE_TABLE_DESCRIPTOR
case|:
name|updateTableDescriptor
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_WRITE_FS_LAYOUT
argument_list|)
expr_stmt|;
break|break;
case|case
name|RESTORE_SNAPSHOT_WRITE_FS_LAYOUT
case|:
name|restoreSnapshot
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_UPDATE_META
argument_list|)
expr_stmt|;
break|break;
case|case
name|RESTORE_SNAPSHOT_UPDATE_META
case|:
name|updateMETA
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|setNextState
argument_list|(
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_RESTORE_ACL
argument_list|)
expr_stmt|;
break|break;
case|case
name|RESTORE_SNAPSHOT_RESTORE_ACL
case|:
name|restoreSnapshotAcl
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
literal|"master-restore-snapshot"
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
literal|"Retriable error trying to restore snapshot="
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
name|RestoreSnapshotState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|state
operator|==
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_PRE_OPERATION
condition|)
block|{
comment|// nothing to rollback
return|return;
block|}
comment|// The restore snapshot doesn't have a rollback. The execution will succeed, at some point.
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
name|RestoreSnapshotState
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|RESTORE_SNAPSHOT_PRE_OPERATION
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
name|RestoreSnapshotState
name|getState
parameter_list|(
specifier|final
name|int
name|stateId
parameter_list|)
block|{
return|return
name|RestoreSnapshotState
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
name|RestoreSnapshotState
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
name|RestoreSnapshotState
name|getInitialState
parameter_list|()
block|{
return|return
name|RestoreSnapshotState
operator|.
name|RESTORE_SNAPSHOT_PRE_OPERATION
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
name|modifiedTableDescriptor
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
comment|// Restore is modifying a table
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
name|RestoreSnapshotStateData
operator|.
name|Builder
name|restoreSnapshotMsg
init|=
name|MasterProcedureProtos
operator|.
name|RestoreSnapshotStateData
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
name|setModifiedTableSchema
argument_list|(
name|ProtobufUtil
operator|.
name|toTableSchema
argument_list|(
name|modifiedTableDescriptor
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionsToRestore
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsToRestore
control|)
block|{
name|restoreSnapshotMsg
operator|.
name|addRegionInfoForRestore
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
if|if
condition|(
name|regionsToRemove
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsToRemove
control|)
block|{
name|restoreSnapshotMsg
operator|.
name|addRegionInfoForRemove
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
if|if
condition|(
name|regionsToAdd
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|regionsToAdd
control|)
block|{
name|restoreSnapshotMsg
operator|.
name|addRegionInfoForAdd
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
name|restoreSnapshotMsg
operator|.
name|addParentToChildRegionsPairList
argument_list|(
name|parentToChildrenPair
argument_list|)
expr_stmt|;
block|}
block|}
name|restoreSnapshotMsg
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
name|RestoreSnapshotStateData
name|restoreSnapshotMsg
init|=
name|MasterProcedureProtos
operator|.
name|RestoreSnapshotStateData
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
name|restoreSnapshotMsg
operator|.
name|getUserInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|snapshot
operator|=
name|restoreSnapshotMsg
operator|.
name|getSnapshot
argument_list|()
expr_stmt|;
name|modifiedTableDescriptor
operator|=
name|ProtobufUtil
operator|.
name|toTableDescriptor
argument_list|(
name|restoreSnapshotMsg
operator|.
name|getModifiedTableSchema
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRestoreCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|regionsToRestore
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|regionsToRestore
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRestoreCount
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
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRestoreList
argument_list|()
control|)
block|{
name|regionsToRestore
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
if|if
condition|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRemoveCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|regionsToRemove
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|regionsToRemove
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRemoveCount
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
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForRemoveList
argument_list|()
control|)
block|{
name|regionsToRemove
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
if|if
condition|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForAddCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|regionsToAdd
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|regionsToAdd
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForAddCount
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
name|restoreSnapshotMsg
operator|.
name|getRegionInfoForAddList
argument_list|()
control|)
block|{
name|regionsToAdd
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
if|if
condition|(
name|restoreSnapshotMsg
operator|.
name|getParentToChildRegionsPairListCount
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|MasterProcedureProtos
operator|.
name|RestoreParentToChildRegionsPair
name|parentToChildrenPair
range|:
name|restoreSnapshotMsg
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
block|}
comment|/**    * Action before any real action of restoring from snapshot.    * @param env MasterProcedureEnv    * @throws IOException    */
specifier|private
name|void
name|prepareRestore
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
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|tableName
argument_list|)
throw|;
block|}
comment|// Check whether table is disabled.
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
comment|// Check that we have at least 1 CF
if|if
condition|(
name|modifiedTableDescriptor
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
comment|// Table already exist. Check and update the region quota for this table namespace.
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
name|int
name|snapshotRegionCount
init|=
name|manifest
operator|.
name|getRegionManifestsMap
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|tableRegionCount
init|=
name|ProcedureSyncWait
operator|.
name|getMasterQuotaManager
argument_list|(
name|env
argument_list|)
operator|.
name|getRegionCountOfTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotRegionCount
operator|>
literal|0
operator|&&
name|tableRegionCount
operator|!=
name|snapshotRegionCount
condition|)
block|{
name|ProcedureSyncWait
operator|.
name|getMasterQuotaManager
argument_list|(
name|env
argument_list|)
operator|.
name|checkAndUpdateNamespaceRegionQuota
argument_list|(
name|tableName
argument_list|,
name|snapshotRegionCount
argument_list|)
expr_stmt|;
block|}
block|}
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
name|modifiedTableDescriptor
argument_list|)
expr_stmt|;
block|}
comment|/**    * Execute the on-disk Restore    * @param env MasterProcedureEnv    * @throws IOException    **/
specifier|private
name|void
name|restoreSnapshot
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|MasterFileSystem
name|fileSystemManager
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|fileSystemManager
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|rootDir
init|=
name|fileSystemManager
operator|.
name|getRootDir
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
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
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConfiguration
argument_list|()
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
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|manifest
argument_list|,
name|modifiedTableDescriptor
argument_list|,
name|rootDir
argument_list|,
name|monitorException
argument_list|,
name|getMonitorStatus
argument_list|()
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
name|regionsToRestore
operator|=
name|metaChanges
operator|.
name|getRegionsToRestore
argument_list|()
expr_stmt|;
name|regionsToRemove
operator|=
name|metaChanges
operator|.
name|getRegionsToRemove
argument_list|()
expr_stmt|;
name|regionsToAdd
operator|=
name|metaChanges
operator|.
name|getRegionsToAdd
argument_list|()
expr_stmt|;
name|parentsToChildrenPairMap
operator|=
name|metaChanges
operator|.
name|getParentToChildrenPairMap
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" failed in on-disk restore. Try re-running the restore command."
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
name|monitorException
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Apply changes to hbase:meta    * @param env MasterProcedureEnv    * @throws IOException    **/
specifier|private
name|void
name|updateMETA
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
name|Connection
name|conn
init|=
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConnection
argument_list|()
decl_stmt|;
comment|// 1. Prepare to restore
name|getMonitorStatus
argument_list|()
operator|.
name|setStatus
argument_list|(
literal|"Preparing to restore each region"
argument_list|)
expr_stmt|;
comment|// 2. Applies changes to hbase:meta
comment|// (2.1). Removes the current set of regions from META
comment|//
comment|// By removing also the regions to restore (the ones present both in the snapshot
comment|// and in the current state) we ensure that no extra fields are present in META
comment|// e.g. with a simple add addRegionToMeta() the splitA and splitB attributes
comment|// not overwritten/removed, so you end up with old informations
comment|// that are not correct after the restore.
if|if
condition|(
name|regionsToRemove
operator|!=
literal|null
condition|)
block|{
name|MetaTableAccessor
operator|.
name|deleteRegions
argument_list|(
name|conn
argument_list|,
name|regionsToRemove
argument_list|)
expr_stmt|;
block|}
comment|// (2.2). Add the new set of regions to META
comment|//
comment|// At this point the old regions are no longer present in META.
comment|// and the set of regions present in the snapshot will be written to META.
comment|// All the information in hbase:meta are coming from the .regioninfo of each region present
comment|// in the snapshot folder.
if|if
condition|(
name|regionsToAdd
operator|!=
literal|null
condition|)
block|{
name|MetaTableAccessor
operator|.
name|addRegionsToMeta
argument_list|(
name|conn
argument_list|,
name|regionsToAdd
argument_list|,
name|modifiedTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regionsToRestore
operator|!=
literal|null
condition|)
block|{
name|MetaTableAccessor
operator|.
name|overwriteRegions
argument_list|(
name|conn
argument_list|,
name|regionsToRestore
argument_list|,
name|modifiedTableDescriptor
operator|.
name|getRegionReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|modifiedTableDescriptor
argument_list|,
name|parentsToChildrenPairMap
argument_list|)
decl_stmt|;
name|metaChanges
operator|.
name|updateMetaParentRegions
argument_list|(
name|conn
argument_list|,
name|regionsToAdd
argument_list|)
expr_stmt|;
comment|// At this point the restore is complete.
name|LOG
operator|.
name|info
argument_list|(
literal|"Restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" on table="
operator|+
name|getTableName
argument_list|()
operator|+
literal|" completed!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
specifier|final
name|ForeignExceptionDispatcher
name|monitorException
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|()
decl_stmt|;
name|String
name|msg
init|=
literal|"restore snapshot="
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" failed in meta update. Try re-running the restore command."
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
name|monitorException
operator|.
name|receive
argument_list|(
operator|new
name|ForeignException
argument_list|(
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|monitorStatus
operator|.
name|markComplete
argument_list|(
literal|"Restore snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"'!"
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
name|addSnapshotRestore
argument_list|(
name|monitorStatus
operator|.
name|getCompletionTimestamp
argument_list|()
operator|-
name|monitorStatus
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|restoreSnapshotAcl
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
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConfiguration
argument_list|()
argument_list|)
condition|)
block|{
comment|// restore acl of snapshot to table.
name|RestoreSnapshotHelper
operator|.
name|restoreSnapshotAcl
argument_list|(
name|snapshot
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
name|snapshot
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|env
operator|.
name|getMasterServices
argument_list|()
operator|.
name|getConfiguration
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
block|}
end_class

end_unit

