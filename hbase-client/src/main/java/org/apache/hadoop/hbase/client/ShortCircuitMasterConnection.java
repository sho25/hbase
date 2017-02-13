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
name|client
package|;
end_package

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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
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
name|protobuf
operator|.
name|ServiceException
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
name|ClientProtos
operator|.
name|CoprocessorServiceRequest
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
name|ClientProtos
operator|.
name|CoprocessorServiceResponse
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
name|MasterProtos
operator|.
name|*
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
name|ReplicationProtos
operator|.
name|AddReplicationPeerRequest
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
name|ReplicationProtos
operator|.
name|AddReplicationPeerResponse
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
name|ReplicationProtos
operator|.
name|DisableReplicationPeerRequest
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
name|ReplicationProtos
operator|.
name|DisableReplicationPeerResponse
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
name|ReplicationProtos
operator|.
name|EnableReplicationPeerRequest
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
name|ReplicationProtos
operator|.
name|EnableReplicationPeerResponse
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
name|ReplicationProtos
operator|.
name|GetReplicationPeerConfigRequest
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
name|ReplicationProtos
operator|.
name|GetReplicationPeerConfigResponse
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
name|ReplicationProtos
operator|.
name|ListReplicationPeersRequest
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
name|ReplicationProtos
operator|.
name|ListReplicationPeersResponse
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
name|ReplicationProtos
operator|.
name|RemoveReplicationPeerRequest
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
name|ReplicationProtos
operator|.
name|RemoveReplicationPeerResponse
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
name|ReplicationProtos
operator|.
name|UpdateReplicationPeerConfigRequest
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
name|ReplicationProtos
operator|.
name|UpdateReplicationPeerConfigResponse
import|;
end_import

begin_comment
comment|/**  * A short-circuit connection that can bypass the RPC layer (serialization, deserialization,  * networking, etc..) when talking to a local master  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ShortCircuitMasterConnection
implements|implements
name|MasterKeepAliveConnection
block|{
specifier|private
specifier|final
name|MasterService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
specifier|public
name|ShortCircuitMasterConnection
parameter_list|(
name|MasterService
operator|.
name|BlockingInterface
name|stub
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|UnassignRegionResponse
name|unassignRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|UnassignRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|unassignRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TruncateTableResponse
name|truncateTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|TruncateTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|truncateTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StopMasterResponse
name|stopMaster
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|StopMasterRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|stopMaster
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SnapshotResponse
name|snapshot
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SnapshotRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|snapshot
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ShutdownResponse
name|shutdown
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ShutdownRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|shutdown
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SetSplitOrMergeEnabledResponse
name|setSplitOrMergeEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SetSplitOrMergeEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|setSplitOrMergeEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SetQuotaResponse
name|setQuota
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SetQuotaRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|setQuota
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SetNormalizerRunningResponse
name|setNormalizerRunning
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SetNormalizerRunningRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|setNormalizerRunning
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SetBalancerRunningResponse
name|setBalancerRunning
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SetBalancerRunningRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|setBalancerRunning
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RunCatalogScanResponse
name|runCatalogScan
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RunCatalogScanRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|runCatalogScan
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RestoreSnapshotResponse
name|restoreSnapshot
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RestoreSnapshotRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|restoreSnapshot
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RemoveReplicationPeerResponse
name|removeReplicationPeer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RemoveReplicationPeerRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|removeReplicationPeer
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RemoveDrainFromRegionServersResponse
name|removeDrainFromRegionServers
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RemoveDrainFromRegionServersRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|removeDrainFromRegionServers
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|OfflineRegionResponse
name|offlineRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|OfflineRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|offlineRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|NormalizeResponse
name|normalize
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|NormalizeRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|normalize
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MoveRegionResponse
name|moveRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MoveRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|moveRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ModifyTableResponse
name|modifyTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ModifyTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|modifyTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ModifyNamespaceResponse
name|modifyNamespace
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ModifyNamespaceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|modifyNamespace
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ModifyColumnResponse
name|modifyColumn
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ModifyColumnRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|modifyColumn
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MergeTableRegionsResponse
name|mergeTableRegions
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MergeTableRegionsRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|mergeTableRegions
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListTableNamesByNamespaceResponse
name|listTableNamesByNamespace
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListTableNamesByNamespaceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listTableNamesByNamespace
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListTableDescriptorsByNamespaceResponse
name|listTableDescriptorsByNamespace
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListTableDescriptorsByNamespaceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listTableDescriptorsByNamespace
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListProceduresResponse
name|listProcedures
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListProceduresRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listProcedures
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListLocksResponse
name|listLocks
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListLocksRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listLocks
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListNamespaceDescriptorsResponse
name|listNamespaceDescriptors
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListNamespaceDescriptorsRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listNamespaceDescriptors
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListDrainingRegionServersResponse
name|listDrainingRegionServers
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListDrainingRegionServersRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listDrainingRegionServers
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsSplitOrMergeEnabledResponse
name|isSplitOrMergeEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsSplitOrMergeEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isSplitOrMergeEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsSnapshotDoneResponse
name|isSnapshotDone
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsSnapshotDoneRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isSnapshotDone
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsProcedureDoneResponse
name|isProcedureDone
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsProcedureDoneRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isProcedureDone
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsNormalizerEnabledResponse
name|isNormalizerEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsNormalizerEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isNormalizerEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsMasterRunningResponse
name|isMasterRunning
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsMasterRunningRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isMasterRunning
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsInMaintenanceModeResponse
name|isMasterInMaintenanceMode
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsInMaintenanceModeRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isMasterInMaintenanceMode
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsCatalogJanitorEnabledResponse
name|isCatalogJanitorEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsCatalogJanitorEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isCatalogJanitorEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsBalancerEnabledResponse
name|isBalancerEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsBalancerEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isBalancerEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetTableStateResponse
name|getTableState
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetTableStateRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getTableState
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetTableNamesResponse
name|getTableNames
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetTableNamesRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getTableNames
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetTableDescriptorsResponse
name|getTableDescriptors
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetTableDescriptorsRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getTableDescriptors
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SecurityCapabilitiesResponse
name|getSecurityCapabilities
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SecurityCapabilitiesRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getSecurityCapabilities
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetSchemaAlterStatusResponse
name|getSchemaAlterStatus
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetSchemaAlterStatusRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getSchemaAlterStatus
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetProcedureResultResponse
name|getProcedureResult
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetProcedureResultRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getProcedureResult
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetNamespaceDescriptorResponse
name|getNamespaceDescriptor
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetNamespaceDescriptorRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getNamespaceDescriptor
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MajorCompactionTimestampResponse
name|getLastMajorCompactionTimestampForRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MajorCompactionTimestampForRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getLastMajorCompactionTimestampForRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MajorCompactionTimestampResponse
name|getLastMajorCompactionTimestamp
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|MajorCompactionTimestampRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getLastMajorCompactionTimestamp
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetCompletedSnapshotsResponse
name|getCompletedSnapshots
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetCompletedSnapshotsRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getCompletedSnapshots
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetClusterStatusResponse
name|getClusterStatus
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetClusterStatusRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getClusterStatus
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecProcedureResponse
name|execProcedureWithRet
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ExecProcedureRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|execProcedureWithRet
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecProcedureResponse
name|execProcedure
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ExecProcedureRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|execProcedure
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoprocessorServiceResponse
name|execMasterService
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CoprocessorServiceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|execMasterService
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|EnableTableResponse
name|enableTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EnableTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|enableTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|EnableReplicationPeerResponse
name|enableReplicationPeer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EnableReplicationPeerRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|enableReplicationPeer
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|EnableCatalogJanitorResponse
name|enableCatalogJanitor
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|EnableCatalogJanitorRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|enableCatalogJanitor
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DrainRegionServersResponse
name|drainRegionServers
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DrainRegionServersRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|drainRegionServers
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DisableTableResponse
name|disableTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DisableTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|disableTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DisableReplicationPeerResponse
name|disableReplicationPeer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DisableReplicationPeerRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|disableReplicationPeer
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DeleteTableResponse
name|deleteTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DeleteTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|deleteTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DeleteSnapshotResponse
name|deleteSnapshot
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DeleteSnapshotRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|deleteSnapshot
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DeleteNamespaceResponse
name|deleteNamespace
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DeleteNamespaceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|deleteNamespace
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DeleteColumnResponse
name|deleteColumn
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|DeleteColumnRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|deleteColumn
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateTableResponse
name|createTable
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CreateTableRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|createTable
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CreateNamespaceResponse
name|createNamespace
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CreateNamespaceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|createNamespace
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BalanceResponse
name|balance
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|BalanceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|balance
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AssignRegionResponse
name|assignRegion
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AssignRegionRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|assignRegion
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AddReplicationPeerResponse
name|addReplicationPeer
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AddReplicationPeerRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|addReplicationPeer
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AddColumnResponse
name|addColumn
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AddColumnRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|addColumn
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AbortProcedureResponse
name|abortProcedure
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|AbortProcedureRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|abortProcedure
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// nothing to do here
block|}
annotation|@
name|Override
specifier|public
name|RunCleanerChoreResponse
name|runCleanerChore
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|RunCleanerChoreRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|runCleanerChore
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SetCleanerChoreRunningResponse
name|setCleanerChoreRunning
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SetCleanerChoreRunningRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|setCleanerChoreRunning
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|IsCleanerChoreEnabledResponse
name|isCleanerChoreEnabled
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|IsCleanerChoreEnabledRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|isCleanerChoreEnabled
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GetReplicationPeerConfigResponse
name|getReplicationPeerConfig
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|GetReplicationPeerConfigRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|getReplicationPeerConfig
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|UpdateReplicationPeerConfigResponse
name|updateReplicationPeerConfig
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|UpdateReplicationPeerConfigRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|updateReplicationPeerConfig
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ListReplicationPeersResponse
name|listReplicationPeers
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|ListReplicationPeersRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
return|return
name|stub
operator|.
name|listReplicationPeers
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
end_class

end_unit

