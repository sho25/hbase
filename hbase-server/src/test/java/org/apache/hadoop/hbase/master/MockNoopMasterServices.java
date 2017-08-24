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
name|hbase
operator|.
name|ChoreService
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
name|CoordinatedStateManager
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
name|ProcedureInfo
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
name|Server
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
name|TableDescriptors
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
name|ClusterConnection
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
name|MasterSwitchType
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
name|executor
operator|.
name|ExecutorService
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
name|favored
operator|.
name|FavoredNodesManager
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
name|locking
operator|.
name|LockManager
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
name|normalizer
operator|.
name|RegionNormalizer
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
name|MasterProcedureEnv
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
name|snapshot
operator|.
name|SnapshotManager
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
name|procedure
operator|.
name|MasterProcedureManagerHost
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
name|LockInfo
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
name|ProcedureEvent
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
name|ProcedureExecutor
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
name|quotas
operator|.
name|MasterQuotaManager
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeerDescription
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
name|ZooKeeperWatcher
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_class
specifier|public
class|class
name|MockNoopMasterServices
implements|implements
name|MasterServices
implements|,
name|Server
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|MetricsMaster
name|metricsMaster
decl_stmt|;
specifier|public
name|MockNoopMasterServices
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MockNoopMasterServices
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|metricsMaster
operator|=
operator|new
name|MetricsMaster
argument_list|(
operator|new
name|MetricsMasterWrapperImpl
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkTableModifiable
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|//no-op
block|}
annotation|@
name|Override
specifier|public
name|long
name|createTable
parameter_list|(
specifier|final
name|TableDescriptor
name|desc
parameter_list|,
specifier|final
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no-op
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|createSystemTable
parameter_list|(
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecutorService
name|getExecutorService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionNormalizer
name|getRegionNormalizer
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogJanitor
name|getCatalogJanitor
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterFileSystem
name|getMasterFileSystem
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterWalManager
name|getMasterWalManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterCoprocessorHost
name|getMasterCoprocessorHost
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterQuotaManager
name|getMasterQuotaManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetricsMaster
name|getMasterMetrics
parameter_list|()
block|{
return|return
name|metricsMaster
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerManager
name|getServerManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetaTableLocator
name|getMetaTableLocator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"mock.master"
argument_list|,
literal|12345
argument_list|,
literal|1
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
comment|//no-op
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isServerCrashProcessingEnabled
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|registerService
parameter_list|(
name|Service
name|instance
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|abortProcedure
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|boolean
name|mayInterruptIfRunning
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ProcedureInfo
argument_list|>
name|listProcedures
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|LockInfo
argument_list|>
name|listLocks
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TableDescriptor
argument_list|>
name|listTableDescriptorsByNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
comment|//To change body of implemented methods use File | Settings | File Templates.
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|TableName
argument_list|>
name|listTableNamesByNamespace
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|deleteTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|truncateTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|boolean
name|preserveSplits
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|modifyTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|TableDescriptor
name|descriptor
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|enableTable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|disableTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|addColumn
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ColumnFamilyDescriptor
name|columnDescriptor
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|modifyColumn
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ColumnFamilyDescriptor
name|descriptor
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|deleteColumn
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|columnName
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|mergeRegions
parameter_list|(
specifier|final
name|HRegionInfo
index|[]
name|regionsToMerge
parameter_list|,
specifier|final
name|boolean
name|forcible
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|splitRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|byte
index|[]
name|splitRow
parameter_list|,
specifier|final
name|long
name|nonceGroup
parameter_list|,
specifier|final
name|long
name|nonce
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableStateManager
name|getTableStateManager
parameter_list|()
block|{
return|return
name|mock
argument_list|(
name|TableStateManager
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isActiveMaster
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isInMaintenanceMode
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastMajorCompactionTimestamp
parameter_list|(
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLastMajorCompactionTimestampForRegion
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterSchema
name|getClusterSchema
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getClusterConnection
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|LoadBalancer
name|getLoadBalancer
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FavoredNodesManager
name|getFavoredNodesManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|SnapshotManager
name|getSnapshotManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MasterProcedureManagerHost
name|getMasterProcedureManagerHost
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSplitOrMergeEnabled
parameter_list|(
name|MasterSwitchType
name|switchType
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|removeReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|enableReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|disableReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|drainRegionServer
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|listDrainingRegionServers
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeDrainFromRegionServer
parameter_list|(
name|ServerName
name|servers
parameter_list|)
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationPeerConfig
name|getReplicationPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateReplicationPeerConfig
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|listReplicationPeers
parameter_list|(
name|String
name|regex
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|LockManager
name|getLockManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRegionServerVersion
parameter_list|(
name|ServerName
name|sn
parameter_list|)
block|{
return|return
literal|"0.0.0"
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkIfShouldMoveSystemRegionAsync
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|recoverMeta
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClientIdAuditPrefix
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ProcedureEvent
name|getInitializedEvent
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

