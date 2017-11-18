begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HBaseIOException
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
name|LockedResource
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
name|Procedure
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
name|annotations
operator|.
name|VisibleForTesting
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

begin_comment
comment|/**  * A curated subset of services provided by {@link HMaster}.  * For use internally only. Passed to Managers, Services and Chores so can pass less-than-a  * full-on HMaster at test-time. Be judicious adding API. Changes cause ripples through  * the code base.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MasterServices
extends|extends
name|Server
block|{
comment|/**    * @return the underlying snapshot manager    */
name|SnapshotManager
name|getSnapshotManager
parameter_list|()
function_decl|;
comment|/**    * @return the underlying MasterProcedureManagerHost    */
name|MasterProcedureManagerHost
name|getMasterProcedureManagerHost
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link ClusterSchema}    */
name|ClusterSchema
name|getClusterSchema
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of the {@link AssignmentManager}    */
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's filesystem {@link MasterFileSystem} utility class.    */
name|MasterFileSystem
name|getMasterFileSystem
parameter_list|()
function_decl|;
comment|/**    * @return Master's WALs {@link MasterWalManager} utility class.    */
name|MasterWalManager
name|getMasterWalManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's {@link ServerManager} instance.    */
name|ServerManager
name|getServerManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link ExecutorService}    */
name|ExecutorService
name|getExecutorService
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link TableStateManager}    */
name|TableStateManager
name|getTableStateManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link MasterCoprocessorHost}    */
name|MasterCoprocessorHost
name|getMasterCoprocessorHost
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link MasterQuotaManager}    */
name|MasterQuotaManager
name|getMasterQuotaManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link RegionNormalizer}    */
name|RegionNormalizer
name|getRegionNormalizer
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link CatalogJanitor}    */
name|CatalogJanitor
name|getCatalogJanitor
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link ProcedureExecutor}    */
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
function_decl|;
comment|/**    * @return Tripped when Master has finished initialization.    */
annotation|@
name|VisibleForTesting
specifier|public
name|ProcedureEvent
name|getInitializedEvent
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link MetricsMaster}    */
name|MetricsMaster
name|getMasterMetrics
parameter_list|()
function_decl|;
comment|/**    * Check table is modifiable; i.e. exists and is offline.    * @param tableName Name of table to check.    * @throws TableNotDisabledException    * @throws TableNotFoundException    * @throws IOException    */
comment|// We actually throw the exceptions mentioned in the
name|void
name|checkTableModifiable
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
throws|,
name|TableNotFoundException
throws|,
name|TableNotDisabledException
function_decl|;
comment|/**    * Create a table using the given table definition.    * @param desc The table definition    * @param splitKeys Starting row keys for the initial table regions.  If null    * @param nonceGroup    * @param nonce    *     a single region is created.    */
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
function_decl|;
comment|/**    * Create a system table using the given table definition.    * @param tableDescriptor The system table definition    *     a single region is created.    */
name|long
name|createSystemTable
parameter_list|(
specifier|final
name|TableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Delete a table    * @param tableName The table name    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Truncate a table    * @param tableName The table name    * @param preserveSplits True if the splits should be preserved    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Modify the descriptor of an existing table    * @param tableName The table name    * @param descriptor The updated table descriptor    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Enable an existing table    * @param tableName The table name    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Disable an existing table    * @param tableName The table name    * @param nonceGroup    * @param nonce    * @throws IOException    */
name|long
name|disableTable
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
function_decl|;
comment|/**    * Add a new column to an existing table    * @param tableName The table name    * @param column The column definition    * @param nonceGroup    * @param nonce    * @throws IOException    */
name|long
name|addColumn
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|ColumnFamilyDescriptor
name|column
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
function_decl|;
comment|/**    * Modify the column descriptor of an existing column in an existing table    * @param tableName The table name    * @param descriptor The updated column definition    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Delete a column from an existing table    * @param tableName The table name    * @param columnName The column name    * @param nonceGroup    * @param nonce    * @throws IOException    */
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
function_decl|;
comment|/**    * Merge regions in a table.    * @param regionsToMerge daughter regions to merge    * @param forcible whether to force to merge even two regions are not adjacent    * @param nonceGroup used to detect duplicate    * @param nonce used to detect duplicate    * @return  procedure Id    * @throws IOException    */
name|long
name|mergeRegions
parameter_list|(
specifier|final
name|RegionInfo
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
function_decl|;
comment|/**    * Split a region.    * @param regionInfo region to split    * @param splitRow split point    * @param nonceGroup used to detect duplicate    * @param nonce used to detect duplicate    * @return  procedure Id    * @throws IOException    */
name|long
name|splitRegion
parameter_list|(
specifier|final
name|RegionInfo
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
function_decl|;
comment|/**    * @return Return table descriptors implementation.    */
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
function_decl|;
comment|/**    * @return true if master enables ServerShutdownHandler;    */
name|boolean
name|isServerCrashProcessingEnabled
parameter_list|()
function_decl|;
comment|/**    * Registers a new protocol buffer {@link Service} subclass as a master coprocessor endpoint.    *    *<p>    * Only a single instance may be registered for a given {@link Service} subclass (the    * instances are keyed on {@link com.google.protobuf.Descriptors.ServiceDescriptor#getFullName()}.    * After the first registration, subsequent calls with the same service name will fail with    * a return value of {@code false}.    *</p>    * @param instance the {@code Service} subclass instance to expose as a coprocessor endpoint    * @return {@code true} if the registration was successful, {@code false}    * otherwise    */
name|boolean
name|registerService
parameter_list|(
name|Service
name|instance
parameter_list|)
function_decl|;
comment|/**    * @return true if master is the active one    */
name|boolean
name|isActiveMaster
parameter_list|()
function_decl|;
comment|/**    * @return true if master is initialized    */
name|boolean
name|isInitialized
parameter_list|()
function_decl|;
comment|/**    * @return true if master is in maintanceMode    */
name|boolean
name|isInMaintenanceMode
parameter_list|()
function_decl|;
comment|/**    * Abort a procedure.    * @param procId ID of the procedure    * @param mayInterruptIfRunning if the proc completed at least one step, should it be aborted?    * @return true if aborted, false if procedure already completed or does not exist    * @throws IOException    */
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
function_decl|;
comment|/**    * Get procedures    * @return procedure list    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|Procedure
argument_list|<
name|?
argument_list|>
argument_list|>
name|getProcedures
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get locks    * @return lock list    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|LockedResource
argument_list|>
name|getLocks
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get list of table descriptors by namespace    * @param name namespace name    * @return descriptors    * @throws IOException    */
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
function_decl|;
comment|/**    * Get list of table names by namespace    * @param name namespace name    * @return table names    * @throws IOException    */
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
function_decl|;
comment|/**    * @param table the table for which last successful major compaction time is queried    * @return the timestamp of the last successful major compaction for the passed table,    * or 0 if no HFile resulting from a major compaction exists    * @throws IOException    */
specifier|public
name|long
name|getLastMajorCompactionTimestamp
parameter_list|(
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @param regionName    * @return the timestamp of the last successful major compaction for the passed region    * or 0 if no HFile resulting from a major compaction exists    * @throws IOException    */
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
function_decl|;
comment|/**    * @return load balancer    */
specifier|public
name|LoadBalancer
name|getLoadBalancer
parameter_list|()
function_decl|;
name|boolean
name|isSplitOrMergeEnabled
parameter_list|(
name|MasterSwitchType
name|switchType
parameter_list|)
function_decl|;
comment|/**    * @return Favored Nodes Manager    */
specifier|public
name|FavoredNodesManager
name|getFavoredNodesManager
parameter_list|()
function_decl|;
comment|/**    * Add a new replication peer for replicating data to slave cluster    * @param peerId a short name that identifies the peer    * @param peerConfig configuration for the replication slave cluster    * @param enabled peer state, true if ENABLED and false if DISABLED    */
name|void
name|addReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|,
name|ReplicationPeerConfig
name|peerConfig
parameter_list|,
name|boolean
name|enabled
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
function_decl|;
comment|/**    * Removes a peer and stops the replication    * @param peerId a short name that identifies the peer    */
name|void
name|removeReplicationPeer
parameter_list|(
name|String
name|peerId
parameter_list|)
throws|throws
name|ReplicationException
throws|,
name|IOException
function_decl|;
comment|/**    * Restart the replication stream to the specified peer    * @param peerId a short name that identifies the peer    */
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
function_decl|;
comment|/**    * Stop the replication stream to the specified peer    * @param peerId a short name that identifies the peer    */
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
function_decl|;
comment|/**    * Returns the configured ReplicationPeerConfig for the specified peer    * @param peerId a short name that identifies the peer    * @return ReplicationPeerConfig for the peer    */
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
function_decl|;
comment|/**    * Update the peerConfig for the specified peer    * @param peerId a short name that identifies the peer    * @param peerConfig new config for the peer    */
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
function_decl|;
comment|/**    * Return a list of replication peers.    * @param regex The regular expression to match peer id    * @return a list of replication peers description    */
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
function_decl|;
comment|/**    * @return {@link LockManager} to lock namespaces/tables/regions.    */
name|LockManager
name|getLockManager
parameter_list|()
function_decl|;
specifier|public
name|String
name|getRegionServerVersion
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
function_decl|;
specifier|public
name|void
name|checkIfShouldMoveSystemRegionAsync
parameter_list|()
function_decl|;
comment|/**    * Recover meta table. Will result in no-op is meta is already initialized. Any code that has    * access to master and requires to access meta during process initialization can call this    * method to make sure meta is initialized.    */
name|boolean
name|recoverMeta
parameter_list|()
throws|throws
name|IOException
function_decl|;
name|String
name|getClientIdAuditPrefix
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

