begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|User
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
comment|/**  * Services Master supplies  */
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
comment|/**    * @return Master's instance of {@link TableLockManager}    */
name|TableLockManager
name|getTableLockManager
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
comment|/**    * @return Master's instance of {@link ProcedureExecutor}    */
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
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
name|HTableDescriptor
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
name|HTableDescriptor
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
name|HColumnDescriptor
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
name|HColumnDescriptor
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
comment|/**    * Merge two regions. The real implementation is on the regionserver, master    * just move the regions together and send MERGE RPC to regionserver    * @param region_a region to merge    * @param region_b region to merge    * @param forcible true if do a compulsory merge, otherwise we will only merge    *          two adjacent regions    * @param user effective user    * @throws IOException    */
name|void
name|dispatchMergingRegions
parameter_list|(
specifier|final
name|HRegionInfo
name|region_a
parameter_list|,
specifier|final
name|HRegionInfo
name|region_b
parameter_list|,
specifier|final
name|boolean
name|forcible
parameter_list|,
specifier|final
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return true if master is initialized    */
name|boolean
name|isInitialized
parameter_list|()
function_decl|;
comment|/**    * Abort a procedure.    * @param procId ID of the procedure    * @param mayInterruptIfRunning if the proc completed at least one step, should it be aborted?    * @return true if aborted, false if procedure already completed or does not exist    * @throws IOException     */
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
comment|/**    * List procedures    * @return procedure list    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|ProcedureInfo
argument_list|>
name|listProcedures
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get list of table descriptors by namespace    * @param name namespace name    * @return descriptors    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|HTableDescriptor
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
block|}
end_interface

end_unit

