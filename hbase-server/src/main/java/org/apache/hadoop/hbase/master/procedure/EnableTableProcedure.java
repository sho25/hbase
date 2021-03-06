begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|List
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
name|Cell
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
name|RegionReplicaUtil
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
name|ProcedureStateSerializer
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
name|EnableTableState
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
name|AbstractStateMachineTableProcedure
argument_list|<
name|EnableTableState
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
name|EnableTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|EnableTableProcedure
parameter_list|()
block|{   }
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableName the table to operate on    */
specifier|public
name|EnableTableProcedure
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
argument_list|(
name|env
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param env MasterProcedureEnv    * @param tableName the table to operate on    */
specifier|public
name|EnableTableProcedure
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|TableName
name|tableName
parameter_list|,
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"{} execute state={}"
argument_list|,
name|this
argument_list|,
name|state
argument_list|)
expr_stmt|;
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
comment|// Get the region replica count. If changed since disable, need to do
comment|// more work assigning.
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
name|TableDescriptor
name|tableDescriptor
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
name|int
name|configuredReplicaCount
init|=
name|tableDescriptor
operator|.
name|getRegionReplication
argument_list|()
decl_stmt|;
comment|// Get regions for the table from memory; get both online and offline regions ('true').
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsOfTable
init|=
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionsOfTable
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// How many replicas do we currently have? Check regions returned from
comment|// in-memory state.
name|int
name|currentMaxReplica
init|=
name|getMaxReplicaId
argument_list|(
name|regionsOfTable
argument_list|)
decl_stmt|;
comment|// Read the META table to know the number of replicas the table currently has.
comment|// If there was a table modification on region replica count then need to
comment|// adjust replica counts here.
name|int
name|replicasFound
init|=
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
condition|?
literal|0
else|:
comment|// TODO: Figure better what to do here for hbase:meta replica.
name|getReplicaCountInMeta
argument_list|(
name|connection
argument_list|,
name|configuredReplicaCount
argument_list|,
name|regionsOfTable
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"replicasFound={} (configuredReplicaCount={} for {}"
argument_list|,
name|replicasFound
argument_list|,
name|configuredReplicaCount
argument_list|,
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentMaxReplica
operator|==
operator|(
name|configuredReplicaCount
operator|-
literal|1
operator|)
condition|)
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
literal|"No change in number of region replicas (configuredReplicaCount={});"
operator|+
literal|" assigning."
argument_list|,
name|configuredReplicaCount
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|currentMaxReplica
operator|>
operator|(
name|configuredReplicaCount
operator|-
literal|1
operator|)
condition|)
block|{
comment|// We have additional regions as the replica count has been decreased. Delete
comment|// those regions because already the table is in the unassigned state
name|LOG
operator|.
name|info
argument_list|(
literal|"The number of replicas "
operator|+
operator|(
name|currentMaxReplica
operator|+
literal|1
operator|)
operator|+
literal|"  is more than the region replica count "
operator|+
name|configuredReplicaCount
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|copyOfRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionInfo
argument_list|>
argument_list|(
name|regionsOfTable
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|regionInfo
range|:
name|copyOfRegions
control|)
block|{
if|if
condition|(
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
operator|>
operator|(
name|configuredReplicaCount
operator|-
literal|1
operator|)
condition|)
block|{
comment|// delete the region from the regionStates
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|deleteRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// remove it from the list of regions of the table
name|LOG
operator|.
name|info
argument_list|(
literal|"Removed replica={} of {}"
argument_list|,
name|regionInfo
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
name|regionsOfTable
operator|.
name|remove
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// the replicasFound is less than the regionReplication
name|LOG
operator|.
name|info
argument_list|(
literal|"Number of replicas has increased. Assigning new region replicas."
operator|+
literal|"The previous replica count was {}. The current replica count is {}."
argument_list|,
operator|(
name|currentMaxReplica
operator|+
literal|1
operator|)
argument_list|,
name|configuredReplicaCount
argument_list|)
expr_stmt|;
name|regionsOfTable
operator|=
name|RegionReplicaUtil
operator|.
name|addReplicas
argument_list|(
name|tableDescriptor
argument_list|,
name|regionsOfTable
argument_list|,
name|currentMaxReplica
operator|+
literal|1
argument_list|,
name|configuredReplicaCount
argument_list|)
expr_stmt|;
block|}
comment|// Assign all the table regions. (including region replicas if added).
comment|// createAssignProcedure will try to retain old assignments if possible.
name|addChildProcedure
argument_list|(
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|createAssignProcedures
argument_list|(
name|regionsOfTable
argument_list|)
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
comment|/**    * @return Count of replicas found reading hbase:meta Region row or zk if    *   asking about the hbase:meta table itself..    */
specifier|private
name|int
name|getReplicaCountInMeta
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|int
name|regionReplicaCount
parameter_list|,
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionsOfTable
parameter_list|)
throws|throws
name|IOException
block|{
name|Result
name|r
init|=
name|MetaTableAccessor
operator|.
name|getCatalogFamilyRow
argument_list|(
name|connection
argument_list|,
name|regionsOfTable
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|replicasFound
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|regionReplicaCount
condition|;
name|i
operator|++
control|)
block|{
comment|// Since we have already added the entries to the META we will be getting only that here
name|List
argument_list|<
name|Cell
argument_list|>
name|columnCells
init|=
name|r
operator|.
name|getColumnCells
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|MetaTableAccessor
operator|.
name|getServerColumn
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|columnCells
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|replicasFound
operator|++
expr_stmt|;
block|}
block|}
return|return
name|replicasFound
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
comment|// the skipTableStateCheck is false so we still need to set it...
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
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
literal|false
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|serialize
argument_list|(
name|enableTableMsg
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
name|EnableTableStateData
name|enableTableMsg
init|=
name|serializer
operator|.
name|deserialize
argument_list|(
name|MasterProcedureProtos
operator|.
name|EnableTableStateData
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
name|enableTableMsg
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
name|enableTableMsg
operator|.
name|getTableName
argument_list|()
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
else|else
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
name|ts
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
name|ts
operator|.
name|isDisabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not DISABLED tableState={}; skipping enable; {}"
argument_list|,
name|ts
operator|.
name|getState
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|setFailure
argument_list|(
literal|"master-enable-table"
argument_list|,
operator|new
name|TableNotDisabledException
argument_list|(
name|ts
operator|.
name|toString
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
name|releaseSyncLatch
argument_list|()
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
name|getUser
argument_list|()
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
comment|/**    * @return Maximum region replica id found in passed list of regions.    */
specifier|private
specifier|static
name|int
name|getMaxReplicaId
parameter_list|(
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
parameter_list|)
block|{
name|int
name|max
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RegionInfo
name|regionInfo
range|:
name|regions
control|)
block|{
if|if
condition|(
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
operator|>
name|max
condition|)
block|{
comment|// Iterating through all the list to identify the highest replicaID region.
comment|// We can stop after checking with the first set of regions??
name|max
operator|=
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|max
return|;
block|}
block|}
end_class

end_unit

