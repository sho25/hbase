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
name|protobuf
operator|.
name|generated
operator|.
name|MasterAdminProtos
operator|.
name|MasterAdminService
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
name|TokenInfo
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
name|KerberosInfo
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
name|MasterAdminProtos
operator|.
name|AddColumnRequest
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
name|MasterAdminProtos
operator|.
name|AddColumnResponse
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
name|MasterAdminProtos
operator|.
name|CatalogScanRequest
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
name|MasterAdminProtos
operator|.
name|CatalogScanResponse
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
name|MasterAdminProtos
operator|.
name|CreateTableRequest
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
name|MasterAdminProtos
operator|.
name|CreateTableResponse
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
name|MasterAdminProtos
operator|.
name|DeleteColumnRequest
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
name|MasterAdminProtos
operator|.
name|DeleteColumnResponse
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
name|MasterAdminProtos
operator|.
name|AssignRegionRequest
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
name|MasterAdminProtos
operator|.
name|AssignRegionResponse
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
name|MasterAdminProtos
operator|.
name|DeleteTableRequest
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
name|MasterAdminProtos
operator|.
name|DeleteTableResponse
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
name|MasterAdminProtos
operator|.
name|DisableTableRequest
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
name|MasterAdminProtos
operator|.
name|DisableTableResponse
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
name|MasterAdminProtos
operator|.
name|EnableCatalogJanitorRequest
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
name|MasterAdminProtos
operator|.
name|EnableCatalogJanitorResponse
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
name|MasterAdminProtos
operator|.
name|EnableTableRequest
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
name|MasterAdminProtos
operator|.
name|EnableTableResponse
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
name|MasterAdminProtos
operator|.
name|IsCatalogJanitorEnabledRequest
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
name|MasterAdminProtos
operator|.
name|IsCatalogJanitorEnabledResponse
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
name|MasterAdminProtos
operator|.
name|ModifyColumnRequest
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
name|MasterAdminProtos
operator|.
name|ModifyColumnResponse
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
name|MasterAdminProtos
operator|.
name|ModifyTableRequest
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
name|MasterAdminProtos
operator|.
name|ModifyTableResponse
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
name|MasterAdminProtos
operator|.
name|MoveRegionRequest
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
name|MasterAdminProtos
operator|.
name|MoveRegionResponse
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
name|MasterAdminProtos
operator|.
name|OfflineRegionRequest
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
name|MasterAdminProtos
operator|.
name|OfflineRegionResponse
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
name|MasterAdminProtos
operator|.
name|SetBalancerRunningRequest
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
name|MasterAdminProtos
operator|.
name|SetBalancerRunningResponse
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
name|MasterAdminProtos
operator|.
name|UnassignRegionRequest
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
name|MasterAdminProtos
operator|.
name|UnassignRegionResponse
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
name|MasterAdminProtos
operator|.
name|BalanceRequest
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
name|MasterAdminProtos
operator|.
name|BalanceResponse
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
name|MasterAdminProtos
operator|.
name|ShutdownRequest
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
name|MasterAdminProtos
operator|.
name|ShutdownResponse
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
name|MasterAdminProtos
operator|.
name|StopMasterRequest
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
name|MasterAdminProtos
operator|.
name|StopMasterResponse
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
name|MasterProtos
operator|.
name|IsMasterRunningRequest
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
name|MasterProtos
operator|.
name|IsMasterRunningResponse
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
name|RpcController
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
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Protocol that a client uses to communicate with the Master (for admin purposes).  */
end_comment

begin_interface
annotation|@
name|KerberosInfo
argument_list|(
name|serverPrincipal
operator|=
literal|"hbase.master.kerberos.principal"
argument_list|)
annotation|@
name|TokenInfo
argument_list|(
literal|"HBASE_AUTH_TOKEN"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|MasterAdminProtocol
extends|extends
name|MasterAdminService
operator|.
name|BlockingInterface
extends|,
name|MasterProtocol
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|1L
decl_stmt|;
comment|/* Column-level */
comment|/**    * Adds a column to the specified table    * @param controller Unused (set to null).    * @param req AddColumnRequest that contains:<br>    * - tableName: table to modify<br>    * - column: column descriptor    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Deletes a column from the specified table. Table must be disabled.    * @param controller Unused (set to null).    * @param req DeleteColumnRequest that contains:<br>    * - tableName: table to alter<br>    * - columnName: column family to remove    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Modifies an existing column on the specified table    * @param controller Unused (set to null).    * @param req ModifyColumnRequest that contains:<br>    * - tableName: table name<br>    * - descriptor: new column descriptor    * @throws IOException e    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/* Region-level */
comment|/**    * Move a region to a specified destination server.    * @param controller Unused (set to null).    * @param req The request that contains:<br>    * - region: The encoded region name; i.e. the hash that makes    * up the region name suffix: e.g. if regionname is    *<code>TestTable,0094429456,1289497600452.527db22f95c8a9e0116f0cc13c680396.</code>,    * then the encoded region name is:<code>527db22f95c8a9e0116f0cc13c680396</code>.<br>    * - destServerName: The servername of the destination regionserver.  If    * passed the empty byte array we'll assign to a random server.  A server name    * is made of host, port and startcode.  Here is an example:    *<code> host187.example.com,60020,1289493121758</code>.    * @throws ServiceException that wraps a UnknownRegionException if we can't find a    * region named<code>encodedRegionName</code>    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Assign a region to a server chosen at random.    * @param controller Unused (set to null).    * @param req contains the region to assign.  Will use existing RegionPlan if one    * found.    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Unassign a region from current hosting regionserver.  Region will then be    * assigned to a regionserver chosen at random.  Region could be reassigned    * back to the same server.  Use {@link #moveRegion(RpcController,MoveRegionRequest}    * if you want to control the region movement.    * @param controller Unused (set to null).    * @param req The request that contains:<br>    * - region: Region to unassign. Will clear any existing RegionPlan    * if one found.<br>    * - force: If true, force unassign (Will remove region from    * regions-in-transition too if present as well as from assigned regions --    * radical!.If results in double assignment use hbck -fix to resolve.    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Offline a region from the assignment manager's in-memory state.  The    * region should be in a closed state and there will be no attempt to    * automatically reassign the region as in unassign.   This is a special    * method, and should only be used by experts or hbck.    * @param controller Unused (set to null).    * @param request OfflineRegionRequest that contains:<br>    * - region: Region to offline.  Will clear any existing RegionPlan    * if one found.    * @throws ServiceException    */
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
function_decl|;
comment|/* Table-level */
comment|/**    * Creates a new table asynchronously.  If splitKeys are specified, then the    * table will be created with an initial set of multiple regions.    * If splitKeys is null, the table will be created with a single region.    * @param controller Unused (set to null).    * @param req CreateTableRequest that contains:<br>    * - tablesSchema: table descriptor<br>    * - splitKeys    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Deletes a table    * @param controller Unused (set to null).    * @param req DeleteTableRequest that contains:<br>    * - tableName: table to delete    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Puts the table on-line (only needed if table has been previously taken offline)    * @param controller Unused (set to null).    * @param req EnableTableRequest that contains:<br>    * - tableName: table to enable    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Take table offline    *    * @param controller Unused (set to null).    * @param req DisableTableRequest that contains:<br>    * - tableName: table to take offline    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Modify a table's metadata    *    * @param controller Unused (set to null).    * @param req ModifyTableRequest that contains:<br>    * - tableName: table to modify<br>    * - tableSchema: new descriptor for table    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/* Cluster-level */
comment|/**    * Shutdown an HBase cluster.    * @param controller Unused (set to null).    * @param request ShutdownRequest    * @return ShutdownResponse    * @throws ServiceException    */
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
function_decl|;
comment|/**    * Stop HBase Master only.    * Does not shutdown the cluster.    * @param controller Unused (set to null).    * @param request StopMasterRequest    * @return StopMasterResponse    * @throws ServiceException    */
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
function_decl|;
comment|/**    * Run the balancer.  Will run the balancer and if regions to move, it will    * go ahead and do the reassignments.  Can NOT run for various reasons.  Check    * logs.    * @param c Unused (set to null).    * @param request BalanceRequest    * @return BalanceResponse that contains:<br>    * - balancerRan: True if balancer ran and was able to tell the region servers to    * unassign all the regions to balance (the re-assignment itself is async),    * false otherwise.    */
annotation|@
name|Override
specifier|public
name|BalanceResponse
name|balance
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|BalanceRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Turn the load balancer on or off.    * @param controller Unused (set to null).    * @param req SetBalancerRunningRequest that contains:<br>    * - on: If true, enable balancer. If false, disable balancer.<br>    * - synchronous: if true, wait until current balance() call, if outstanding, to return.    * @return SetBalancerRunningResponse that contains:<br>    * - prevBalanceValue: Previous balancer value    * @throws ServiceException    */
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
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * @param c Unused (set to null).    * @param req IsMasterRunningRequest    * @return IsMasterRunningRequest that contains:<br>    * isMasterRunning: true if master is available    * @throws ServiceException    */
annotation|@
name|Override
specifier|public
name|IsMasterRunningResponse
name|isMasterRunning
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|IsMasterRunningRequest
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Run a scan of the catalog table    * @param c Unused (set to null).    * @param req CatalogScanRequest    * @return CatalogScanResponse that contains the int return code corresponding    *         to the number of entries cleaned    * @throws ServiceException    */
annotation|@
name|Override
specifier|public
name|CatalogScanResponse
name|runCatalogScan
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|CatalogScanRequest
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Enable/Disable the catalog janitor    * @param c Unused (set to null).    * @param req EnableCatalogJanitorRequest that contains:<br>    * - enable: If true, enable catalog janitor. If false, disable janitor.<br>    * @return EnableCatalogJanitorResponse that contains:<br>    * - prevValue: true, if it was enabled previously; false, otherwise    * @throws ServiceException    */
annotation|@
name|Override
specifier|public
name|EnableCatalogJanitorResponse
name|enableCatalogJanitor
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|EnableCatalogJanitorRequest
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
comment|/**    * Query whether the catalog janitor is enabled    * @param c Unused (set to null).    * @param req IsCatalogJanitorEnabledRequest    * @return IsCatalogCatalogJanitorEnabledResponse that contains:<br>    * - value: true, if it is enabled; false, otherwise    * @throws ServiceException    */
annotation|@
name|Override
specifier|public
name|IsCatalogJanitorEnabledResponse
name|isCatalogJanitorEnabled
parameter_list|(
name|RpcController
name|c
parameter_list|,
name|IsCatalogJanitorEnabledRequest
name|req
parameter_list|)
throws|throws
name|ServiceException
function_decl|;
block|}
end_interface

end_unit

