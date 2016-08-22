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
name|regionserver
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ConcurrentMap
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
name|hbase
operator|.
name|HBaseInterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|ipc
operator|.
name|RpcServerInterface
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
name|TableLockManager
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
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
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
name|RegionServerQuotaManager
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
name|regionserver
operator|.
name|throttle
operator|.
name|ThroughputController
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
name|wal
operator|.
name|WAL
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
comment|/**  * Services provided by {@link HRegionServer}  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|RegionServerServices
extends|extends
name|OnlineRegions
extends|,
name|FavoredNodesForRegion
block|{
comment|/**    * @return True if this regionserver is stopping.    */
name|boolean
name|isStopping
parameter_list|()
function_decl|;
comment|/** @return the WAL for a particular region. Pass null for getting the    * default (common) WAL */
name|WAL
name|getWAL
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** @return the List of WALs that are used by this server    *  Doesn't include the meta WAL    */
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Implementation of {@link CompactionRequestor} or null.    */
name|CompactionRequestor
name|getCompactionRequester
parameter_list|()
function_decl|;
comment|/**    * @return Implementation of {@link FlushRequester} or null.    */
name|FlushRequester
name|getFlushRequester
parameter_list|()
function_decl|;
comment|/**    * @return the RegionServerAccounting for this Region Server    */
name|RegionServerAccounting
name|getRegionServerAccounting
parameter_list|()
function_decl|;
comment|/**    * @return RegionServer's instance of {@link TableLockManager}    */
name|TableLockManager
name|getTableLockManager
parameter_list|()
function_decl|;
comment|/**    * @return RegionServer's instance of {@link RegionServerQuotaManager}    */
name|RegionServerQuotaManager
name|getRegionServerQuotaManager
parameter_list|()
function_decl|;
comment|/**    * @return RegionServer's instance of {@link SecureBulkLoadManager}    */
name|SecureBulkLoadManager
name|getSecureBulkLoadManager
parameter_list|()
function_decl|;
comment|/**    * Context for postOpenDeployTasks().    */
class|class
name|PostOpenDeployContext
block|{
specifier|private
specifier|final
name|Region
name|region
decl_stmt|;
specifier|private
specifier|final
name|long
name|masterSystemTime
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|PostOpenDeployContext
parameter_list|(
name|Region
name|region
parameter_list|,
name|long
name|masterSystemTime
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|masterSystemTime
operator|=
name|masterSystemTime
expr_stmt|;
block|}
specifier|public
name|Region
name|getRegion
parameter_list|()
block|{
return|return
name|region
return|;
block|}
specifier|public
name|long
name|getMasterSystemTime
parameter_list|()
block|{
return|return
name|masterSystemTime
return|;
block|}
block|}
comment|/**    * Tasks to perform after region open to complete deploy of region on    * regionserver    *    * @param context the context    * @throws KeeperException    * @throws IOException    */
name|void
name|postOpenDeployTasks
parameter_list|(
specifier|final
name|PostOpenDeployContext
name|context
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
function_decl|;
comment|/**    * Tasks to perform after region open to complete deploy of region on    * regionserver    *    * @param r Region to open.    * @throws KeeperException    * @throws IOException    * @deprecated use {@link #postOpenDeployTasks(PostOpenDeployContext)}    */
annotation|@
name|Deprecated
name|void
name|postOpenDeployTasks
parameter_list|(
specifier|final
name|Region
name|r
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
function_decl|;
class|class
name|RegionStateTransitionContext
block|{
specifier|private
specifier|final
name|TransitionCode
name|code
decl_stmt|;
specifier|private
specifier|final
name|long
name|openSeqNum
decl_stmt|;
specifier|private
specifier|final
name|long
name|masterSystemTime
decl_stmt|;
specifier|private
specifier|final
name|HRegionInfo
index|[]
name|hris
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|RegionStateTransitionContext
parameter_list|(
name|TransitionCode
name|code
parameter_list|,
name|long
name|openSeqNum
parameter_list|,
name|long
name|masterSystemTime
parameter_list|,
name|HRegionInfo
modifier|...
name|hris
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|openSeqNum
operator|=
name|openSeqNum
expr_stmt|;
name|this
operator|.
name|masterSystemTime
operator|=
name|masterSystemTime
expr_stmt|;
name|this
operator|.
name|hris
operator|=
name|hris
expr_stmt|;
block|}
specifier|public
name|TransitionCode
name|getCode
parameter_list|()
block|{
return|return
name|code
return|;
block|}
specifier|public
name|long
name|getOpenSeqNum
parameter_list|()
block|{
return|return
name|openSeqNum
return|;
block|}
specifier|public
name|long
name|getMasterSystemTime
parameter_list|()
block|{
return|return
name|masterSystemTime
return|;
block|}
specifier|public
name|HRegionInfo
index|[]
name|getHris
parameter_list|()
block|{
return|return
name|hris
return|;
block|}
block|}
comment|/**    * Notify master that a handler requests to change a region state    */
name|boolean
name|reportRegionStateTransition
parameter_list|(
specifier|final
name|RegionStateTransitionContext
name|context
parameter_list|)
function_decl|;
comment|/**    * Notify master that a handler requests to change a region state    * @deprecated use {@link #reportRegionStateTransition(RegionStateTransitionContext)}    */
annotation|@
name|Deprecated
name|boolean
name|reportRegionStateTransition
parameter_list|(
name|TransitionCode
name|code
parameter_list|,
name|long
name|openSeqNum
parameter_list|,
name|HRegionInfo
modifier|...
name|hris
parameter_list|)
function_decl|;
comment|/**    * Notify master that a handler requests to change a region state    * @deprecated use {@link #reportRegionStateTransition(RegionStateTransitionContext)}    */
annotation|@
name|Deprecated
name|boolean
name|reportRegionStateTransition
parameter_list|(
name|TransitionCode
name|code
parameter_list|,
name|HRegionInfo
modifier|...
name|hris
parameter_list|)
function_decl|;
comment|/**    * Returns a reference to the region server's RPC server    */
name|RpcServerInterface
name|getRpcServer
parameter_list|()
function_decl|;
comment|/**    * Get the regions that are currently being opened or closed in the RS    * @return map of regions in transition in this RS    */
name|ConcurrentMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Boolean
argument_list|>
name|getRegionsInTransitionInRS
parameter_list|()
function_decl|;
comment|/**    * @return Return the FileSystem object used by the regionserver    */
name|FileSystem
name|getFileSystem
parameter_list|()
function_decl|;
comment|/**    * @return The RegionServer's "Leases" service    */
name|Leases
name|getLeases
parameter_list|()
function_decl|;
comment|/**    * @return hbase executor service    */
name|ExecutorService
name|getExecutorService
parameter_list|()
function_decl|;
comment|/**    * @return set of recovering regions on the hosting region server    */
name|Map
argument_list|<
name|String
argument_list|,
name|Region
argument_list|>
name|getRecoveringRegions
parameter_list|()
function_decl|;
comment|/**    * Only required for "old" log replay; if it's removed, remove this.    * @return The RegionServer's NonceManager    */
specifier|public
name|ServerNonceManager
name|getNonceManager
parameter_list|()
function_decl|;
comment|/**    * @return all the online tables in this RS    */
name|Set
argument_list|<
name|TableName
argument_list|>
name|getOnlineTables
parameter_list|()
function_decl|;
comment|/**    * Registers a new protocol buffer {@link Service} subclass as a coprocessor endpoint to be    * available for handling    * @param service the {@code Service} subclass instance to expose as a coprocessor endpoint    * @return {@code true} if the registration was successful, {@code false}    */
name|boolean
name|registerService
parameter_list|(
name|Service
name|service
parameter_list|)
function_decl|;
comment|/**    * @return heap memory manager instance    */
name|HeapMemoryManager
name|getHeapMemoryManager
parameter_list|()
function_decl|;
comment|/**    * @return the max compaction pressure of all stores on this regionserver. The value should be    *         greater than or equal to 0.0, and any value greater than 1.0 means we enter the    *         emergency state that some stores have too many store files.    * @see org.apache.hadoop.hbase.regionserver.Store#getCompactionPressure()    */
name|double
name|getCompactionPressure
parameter_list|()
function_decl|;
comment|/**    * @return the controller to avoid flush too fast    */
name|ThroughputController
name|getFlushThroughputController
parameter_list|()
function_decl|;
comment|/**    * @return the flush pressure of all stores on this regionserver. The value should be greater than    *         or equal to 0.0, and any value greater than 1.0 means we enter the emergency state that    *         global memstore size already exceeds lower limit.    */
name|double
name|getFlushPressure
parameter_list|()
function_decl|;
comment|/**    * @return the metrics tracker for the region server    */
name|MetricsRegionServer
name|getMetrics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

